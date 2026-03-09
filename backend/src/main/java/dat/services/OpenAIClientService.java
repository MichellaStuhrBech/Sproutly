package dat.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dat.utils.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Calls OpenAI Chat Completions API. API key is read from environment variable
 * OPENAI_API_KEY or from config.properties (OPENAI_API_KEY) if env is not set.
 */
public class OpenAIClientService {

    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    private final String apiKey;
    private final ObjectMapper objectMapper = Utils.getObjectMapper();

    public OpenAIClientService() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            try {
                key = Utils.getPropertyValue("OPENAI_API_KEY", "config.properties");
            } catch (Exception e) {
                key = null;
            }
        }
        this.apiKey = key;
    }

    /**
     * Sends a chat request with a system prompt and user message.
     *
     * @param systemPrompt Optional system message (can be null).
     * @param userMessage  The user's message.
     * @return The assistant's reply text, or null on error or missing API key.
     */
    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        if (userMessage == null || userMessage.isBlank()) {
            return null;
        }
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", MODEL);
            ArrayNode messages = objectMapper.createArrayNode();
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                messages.addObject()
                        .put("role", "system")
                        .put("content", systemPrompt);
            }
            messages.addObject()
                    .put("role", "user")
                    .put("content", userMessage);
            body.set("messages", messages);

            String bodyJson = objectMapper.writeValueAsString(body);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_CHAT_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return null;
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }
            JsonNode first = choices.get(0);
            JsonNode message = first.path("message");
            JsonNode content = message.path("content");
            return content.isTextual() ? content.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
