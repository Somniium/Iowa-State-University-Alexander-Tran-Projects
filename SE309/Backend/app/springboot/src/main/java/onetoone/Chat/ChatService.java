package onetoone.Chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final AiProvider aiProvider;
    private final CyvalContextBuilder contextBuilder;

    @Autowired
    public ChatService(ChatSessionRepository sessionRepo,
                       ChatMessageRepository messageRepo,
                       AiProvider aiProvider,
                       CyvalContextBuilder contextBuilder) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.aiProvider = aiProvider;
        this.contextBuilder = contextBuilder;
    }

    /**
     * NOT @Transactional on purpose. The Gemini call below is a remote HTTP round-trip
     * that can take many seconds; holding a DB connection (and any row locks) open for
     * the entire round-trip would drain the connection pool under load. Each
     * messageRepo.save / sessionRepo.save runs in its own short transaction (Spring Data
     * repositories are @Transactional by default) which is the correct granularity here.
     *
     * Failure semantics: if Gemini fails after the user message is saved, the user
     * message stays in the DB. That's the behavior we want — the user sees their
     * message persisted with no reply, and can retry, instead of losing what they typed.
     */
    public ChatMessage sendMessage(Long userId, Long sessionId, String userText) {
        ChatSession session = (sessionId == null)
                ? createSession(userId, deriveTitle(userText))
                : sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // Save user message (own transaction)
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setRole(ChatMessage.Role.USER);
        userMsg.setContent(userText);
        messageRepo.save(userMsg);

        // Build context-aware system prompt
        String systemPrompt = contextBuilder.buildSystemPrompt(userId, userText);

        // Load history AFTER saving user message — this includes the current turn,
        // so we don't double-send it to the provider.
        List<ChatMessage> history = messageRepo.findBySession_IdOrderByCreatedAtAsc(session.getId());

        // Call LLM. No DB connection held during this network round-trip.
        String reply = aiProvider.complete(systemPrompt, history, userText);

        // Save assistant message (own transaction)
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSession(session);
        assistantMsg.setRole(ChatMessage.Role.ASSISTANT);
        assistantMsg.setContent(reply);
        messageRepo.save(assistantMsg);

        // Touch session
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepo.save(session);

        return assistantMsg;
    }

    public ChatSession createSession(Long userId, String title) {
        ChatSession s = new ChatSession();
        s.setUserId(userId);
        s.setTitle(title);
        return sessionRepo.save(s);
    }

    public List<ChatSession> listSessions(Long userId) {
        return sessionRepo.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public List<ChatMessage> getHistory(Long sessionId) {
        return messageRepo.findBySession_IdOrderByCreatedAtAsc(sessionId);
    }

    private String deriveTitle(String firstMessage) {
        return firstMessage.length() > 60 ? firstMessage.substring(0, 57) + "..." : firstMessage;
    }
}