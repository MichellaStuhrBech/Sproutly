package dat.services;

import dat.dtos.PerenualSpeciesDTO;
import dat.dtos.TreflePlantDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates plant-related chat: fetches plant context from Trefle and Perenual APIs
 * and uses OpenAI to generate a reply grounded in that data.
 */
public class PlantChatService {

    private static final String SYSTEM_PROMPT_PREFIX =
            "You are a helpful plant and gardening assistant for the Sproutly app. "
                    + "Answer only about plants and gardening. If the user asks something off-topic, 
                    politely redirect to plant topics. "
                    + "You have access to: (1) the user's own sowing list (plants they are planning/have added), 
                    (2) the user's todo list (garden tasks), "
                    + "(3) general plant data from Trefle and Perenual. 
                    Use the user's own lists when answering questions about their plants or tasks.\n\n";

    private final TrefleService trefleService;
    private final PerenualService perenualService;
    private final OpenAIClientService openAIClientService;

    public PlantChatService() {
        this(new TrefleService(), new PerenualService(), new OpenAIClientService());
    }

    /**
     * Constructor for testing: inject services to avoid real HTTP calls.
     */
    PlantChatService(TrefleService trefleService, PerenualService perenualService, OpenAIClientService openAIClientService) {
        this.trefleService = trefleService;
        this.perenualService = perenualService;
        this.openAIClientService = openAIClientService;
    }

    /**
     * Answers the user's question using the user's plants/tasks, Trefle and Perenual data, and OpenAI.
     *
     * @param userMessage       The user's message.
     * @param userEmail         User email (for logging; list data is passed explicitly).
     * @param userPlantsSummary Summary of the user's sowing list (plants), or null/empty if none.
     * @param userTasksSummary  Summary of the user's todo list (tasks), or null/empty if none.
     * @return The assistant's reply, or an error message if the AI is unavailable.
     */
    public String answer(String userMessage, String userEmail, String userPlantsSummary, String userTasksSummary) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Please ask a question about plants or gardening.";
        }
        String query = userMessage.trim();
        List<TreflePlantDTO> treflePlants = trefleService.search(query);
        List<PerenualSpeciesDTO> perenualPlants = perenualService.search(query);

        String trefleBlock = formatTrefleContext(treflePlants);
        String perenualBlock = formatPerenualContext(perenualPlants);

        StringBuilder contextBlock = new StringBuilder();
        contextBlock.append("--- User's sowing list (plants they have added) ---\n");
        contextBlock.append(userPlantsSummary != null && !userPlantsSummary.isBlank() ? userPlantsSummary : "(No plants in list yet.)");
        contextBlock.append("\n\n--- User's garden todo list ---\n");
        contextBlock.append(userTasksSummary != null && !userTasksSummary.isBlank() ? userTasksSummary : "(No tasks yet.)");
        contextBlock.append("\n\n--- From Trefle (general plant data) ---\n").append(trefleBlock);
        contextBlock.append("\n\n--- From Perenual (general plant data) ---\n").append(perenualBlock);

        String systemPrompt = SYSTEM_PROMPT_PREFIX + contextBlock;

        String reply = openAIClientService.chat(systemPrompt, userMessage.trim());
        if (reply == null || reply.isBlank()) {
            return "I'm sorry, I couldn't get a response right now. Please check that the chat service is configured and try again.";
        }
        return reply.trim();
    }

    private static String formatTrefleContext(List<TreflePlantDTO> plants) {
        if (plants == null || plants.isEmpty()) {
            return "(No specific plant data found for this query.)";
        }
        return plants.stream()
                .limit(10)
                .map(p -> {
                    String common = p.getCommonName() != null ? p.getCommonName() : "";
                    String scientific = p.getScientificName() != null ? p.getScientificName() : "";
                    return "- " + common + (scientific.isEmpty() ? "" : " (" + scientific + ")");
                })
                .collect(Collectors.joining("\n"));
    }

    private static String formatPerenualContext(List<PerenualSpeciesDTO> plants) {
        if (plants == null || plants.isEmpty()) {
            return "(No specific plant data found for this query.)";
        }
        return plants.stream()
                .limit(10)
                .map(p -> {
                    String common = p.getCommonName() != null ? p.getCommonName() : "";
                    String scientific = p.getScientificName() != null && !p.getScientificName().isEmpty()
                            ? String.join(", ", p.getScientificName())
                            : "";
                    return "- " + common + (scientific.isEmpty() ? "" : " (" + scientific + ")");
                })
                .collect(Collectors.joining("\n"));
    }
}
