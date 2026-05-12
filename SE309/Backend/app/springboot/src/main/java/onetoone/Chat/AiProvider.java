package onetoone.Chat;

import java.util.List;

public interface AiProvider {
    String complete(String systemPrompt, List<ChatMessage> history, String userMessage);
}