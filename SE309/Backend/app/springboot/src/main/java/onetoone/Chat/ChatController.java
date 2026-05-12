package onetoone.Chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    /** Hard cap on a single user message. Prevents callers from blowing through Gemini quota
     *  with a single megabyte-sized prompt and from blowing up the DB row. */
    private static final int MAX_MESSAGE_CHARS = 4000;

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequest req) {
        // Validate request shape up front. Anything we reject here never makes it to the LLM provider.
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing request body"));
        }
        if (req.userId() == null || req.userId() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }
        if (req.message() == null || req.message().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message cannot be empty"));
        }
        if (req.message().length() > MAX_MESSAGE_CHARS) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "message too long",
                    "maxChars", MAX_MESSAGE_CHARS,
                    "actualChars", req.message().length()
            ));
        }

        try {
            ChatMessage reply = chatService.sendMessage(req.userId(), req.sessionId(), req.message());
            return ResponseEntity.ok(Map.of(
                    "sessionId", reply.getSessionId(),
                    "messageId", reply.getId(),
                    "content", reply.getContent(),
                    "createdAt", reply.getCreatedAt()
            ));
        } catch (IllegalArgumentException e) {
            // Caller-fault: bad sessionId, etc. Safe to echo back.
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Server-fault: log full detail server-side, return a generic message to the caller.
            // Previously we echoed e.getMessage() which could leak Gemini API responses (containing
            // API-key-prefixed error text), DB error fragments, etc.
            log.error("Chat failed for userId={} sessionId={}", req.userId(), req.sessionId(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Chat service unavailable"));
        }
    }

    @GetMapping("/sessions/{userId}")
    public ResponseEntity<List<ChatSession>> listSessions(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.listSessions(userId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getHistory(sessionId));
    }

    public record SendMessageRequest(Long userId, Long sessionId, String message) {}
}
