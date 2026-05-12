package onetoone.Chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

/**
 * Calls Google's Gemini API for chat completions.
 *
 * Docs: https://ai.google.dev/api/generate-content
 *
 * Endpoint: POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}
 *
 * Free tier (as of April 2026): 15 requests/minute on Gemini 2.5 Flash.
 */
@Component
public class GeminiProvider implements AiProvider {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String model;

    /**
     * RestTemplate with explicit timeouts. Without these, a slow/hanging Gemini call
     * pins the Tomcat worker thread indefinitely — under load that drains the thread
     * pool and the whole app stops responding. 5s to connect, 30s to read a reply.
     */
    private final RestTemplate restTemplate = buildRestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        return new RestTemplate(factory);
    }

    @Override
    public String complete(String systemPrompt, List<ChatMessage> history, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return "[Gemini not configured — set ai.gemini.api-key in application.properties]";
        }

        try {
            // Build request body. Gemini's format:
            // {
            //   "system_instruction": { "parts": [{ "text": "..." }] },
            //   "contents": [
            //     { "role": "user",  "parts": [{ "text": "..." }] },
            //     { "role": "model", "parts": [{ "text": "..." }] }
            //   ]
            // }
            ObjectNode body = mapper.createObjectNode();

            // System prompt
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                ObjectNode systemInstruction = body.putObject("system_instruction");
                ArrayNode systemParts = systemInstruction.putArray("parts");
                systemParts.addObject().put("text", systemPrompt);
            }

            // Conversation history. Gemini uses "user" and "model" (not "assistant").
            // The current user message is already the last entry in `history` (we save before calling),
            // so we send `history` directly and do NOT append `userMessage` again.
            ArrayNode contents = body.putArray("contents");
            for (ChatMessage msg : history) {
                if (msg.getRole() == ChatMessage.Role.SYSTEM) continue; // handled above
                ObjectNode entry = contents.addObject();
                entry.put("role", msg.getRole() == ChatMessage.Role.USER ? "user" : "model");
                ArrayNode parts = entry.putArray("parts");
                parts.addObject().put("text", msg.getContent());
            }

            // Optional generation config — sane defaults
            ObjectNode genConfig = body.putObject("generationConfig");
            genConfig.put("maxOutputTokens", 1024);
            genConfig.put("temperature", 0.7);

            // POST to Gemini
            String url = BASE_URL + model + ":generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), headers);

            String response = restTemplate.postForObject(url, request, String.class);
            JsonNode root = mapper.readTree(response);

            // Response shape:
            // { "candidates": [ { "content": { "parts": [ { "text": "..." } ] } } ] }
            JsonNode candidates = root.get("candidates");
            if (candidates == null || !candidates.isArray() || candidates.size() == 0) {
                return "[Gemini returned no candidates — possibly blocked by safety filters]";
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.size() == 0) {
                return "[Gemini response had no text parts]";
            }

            StringBuilder sb = new StringBuilder();
            for (JsonNode part : parts) {
                String text = part.path("text").asText("");
                if (!text.isEmpty()) sb.append(text);
            }
            String result = sb.toString();
            return result.isBlank() ? "[Empty response from Gemini]" : result;

        } catch (Exception e) {
            return "[Gemini call failed: " + e.getMessage() + "]";
        }
    }
}