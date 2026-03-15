package dat.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for OpenAIClientService (no real HTTP calls).
 */
class OpenAIClientServiceTest {

    @Test
    void chat_noApiKey_returnsNull() {
        OpenAIClientService service = new OpenAIClientService(null);

        assertNull(service.chat("You are helpful.", "What is a tomato?"));
    }

    @Test
    void chat_blankApiKey_returnsNull() {
        OpenAIClientService service = new OpenAIClientService("");

        assertNull(service.chat("You are helpful.", "What is a tomato?"));
    }

    @Test
    void chat_nullUserMessage_returnsNull() {
        OpenAIClientService service = new OpenAIClientService("dummy-key");

        assertNull(service.chat("System", null));
        assertNull(service.chat("System", ""));
        assertNull(service.chat("System", "   "));
    }
}
