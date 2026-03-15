package dat.services;

import dat.dtos.PerenualSpeciesDTO;
import dat.dtos.TreflePlantDTO;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for PlantChatService using stub services (no real HTTP calls).
 */
class PlantChatServiceTest {

    private static final TrefleService NO_TREFLE = new TrefleService() {
        @Override
        public List<TreflePlantDTO> search(String query) {
            return Collections.emptyList();
        }
    };

    private static final PerenualService NO_PERENUAL = new PerenualService() {
        @Override
        public List<PerenualSpeciesDTO> search(String query) {
            return Collections.emptyList();
        }
    };

    @Test
    void answer_blankMessage_returnsPromptToAskQuestion() {
        OpenAIClientService neverCalled = new OpenAIClientService("dummy-key");
        PlantChatService service = new PlantChatService(NO_TREFLE, NO_PERENUAL, neverCalled);

        assertEquals("Please ask a question about plants or gardening.", service.answer(null, "u@test.dk", "", ""));
        assertEquals("Please ask a question about plants or gardening.", service.answer("", "u@test.dk", "", ""));
        assertEquals("Please ask a question about plants or gardening.", service.answer("   ", "u@test.dk", "", ""));
    }

    @Test
    void answer_openAiReturnsNull_returnsFallbackMessage() {
        OpenAIClientService returnsNull = new OpenAIClientService(null) {
            @Override
            public String chat(String systemPrompt, String userMessage) {
                return null;
            }
        };
        PlantChatService service = new PlantChatService(NO_TREFLE, NO_PERENUAL, returnsNull);

        String result = service.answer("What is a tomato?", "u@test.dk", "", "");

        assertEquals("I'm sorry, I couldn't get a response right now. Please check that the chat service is configured and try again.", result);
    }

    @Test
    void answer_openAiReturnsBlank_returnsFallbackMessage() {
        OpenAIClientService returnsBlank = new OpenAIClientService("key") {
            @Override
            public String chat(String systemPrompt, String userMessage) {
                return "   ";
            }
        };
        PlantChatService service = new PlantChatService(NO_TREFLE, NO_PERENUAL, returnsBlank);

        String result = service.answer("What is a tomato?", "u@test.dk", "", "");

        assertEquals("I'm sorry, I couldn't get a response right now. Please check that the chat service is configured and try again.", result);
    }
}
