package dat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.dtos.PerenualSearchResponse;
import dat.dtos.PerenualSpeciesDTO;
import dat.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Calls Perenual API (species-list) for plant search. API key is read from
 * environment variable PERENUAL_API_KEY or from config.properties if env is not set.
 */
public class PerenualService {

    private static final Logger logger = LoggerFactory.getLogger(PerenualService.class);
    private static final String PERENUAL_SPECIES_LIST = "https://www.perenual.com/api/v2/species-list";

    private final String apiKey;
    private final ObjectMapper objectMapper = Utils.getObjectMapper();

    public PerenualService() {
        this.apiKey = loadApiKeyFromConfig();
        if (this.apiKey == null || this.apiKey.isBlank()) {
            logger.warn("PERENUAL_API_KEY is not set. Set it in the environment, in config.properties, or in a .env file in the project root. Plant search and chatbot Perenual data will be empty.");
        }
    }

    /**
     * Load key from env, then config.properties, then .env (same order as OpenAIClientService).
     */
    private static String loadApiKeyFromConfig() {
        String key = System.getenv("PERENUAL_API_KEY");
        if (key == null || key.isBlank()) {
            try {
                key = Utils.getPropertyValue("PERENUAL_API_KEY", "config.properties");
            } catch (Exception e) {
                key = null;
            }
        }
        if (key == null || key.isBlank()) {
            key = Utils.getEnvFromDotEnv("PERENUAL_API_KEY");
        }
        return key;
    }

    /**
     * Search plant species by keyword.
     *
     * @param query Search query (e.g. plant name).
     * @return List of matching species, or empty list if key missing, query empty, or on error.
     */
    public List<PerenualSpeciesDTO> search(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            logger.debug("Perenual search skipped: no API key");
            return Collections.emptyList();
        }
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = PERENUAL_SPECIES_LIST + "?key=" + apiKey + "&q=" + encoded;
            var client = java.net.http.HttpClient.newHttpClient();
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                logger.warn("Perenual API returned status {} for query '{}'. Response: {}", response.statusCode(), query, response.body().length() > 200 ? response.body().substring(0, 200) + "..." : response.body());
                return Collections.emptyList();
            }
            PerenualSearchResponse parsed = objectMapper.readValue(response.body(), PerenualSearchResponse.class);
            List<PerenualSpeciesDTO> data = parsed.getData() != null ? parsed.getData() : Collections.emptyList();
            logger.debug("Perenual search '{}' returned {} results", query, data.size());
            return data;
        } catch (Exception e) {
            logger.warn("Perenual search failed for '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private static final String PERENUAL_SPECIES_DETAILS = "https://www.perenual.com/api/v2/species/details/";

    /**
     * Fetch full details for a species by id.
     *
     * @param speciesId Perenual species id.
     * @return Details DTO or null if key missing, id invalid, or on error.
     */
    public dat.dtos.PerenualSpeciesDetailsDTO getDetails(int speciesId) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        try {
            String url = PERENUAL_SPECIES_DETAILS + speciesId + "?key=" + apiKey;
            var client = java.net.http.HttpClient.newHttpClient();
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return null;
            }
            return objectMapper.readValue(response.body(), dat.dtos.PerenualSpeciesDetailsDTO.class);
        } catch (Exception e) {
            return null;
        }
    }
}
