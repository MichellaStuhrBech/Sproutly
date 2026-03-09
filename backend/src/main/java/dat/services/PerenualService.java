package dat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.dtos.PerenualSearchResponse;
import dat.dtos.PerenualSpeciesDTO;
import dat.utils.Utils;

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

    private static final String PERENUAL_SPECIES_LIST = "https://www.perenual.com/api/v2/species-list";

    private final String apiKey;
    private final ObjectMapper objectMapper = Utils.getObjectMapper();

    public PerenualService() {
        String key = System.getenv("PERENUAL_API_KEY");
        if (key == null || key.isBlank()) {
            try {
                key = Utils.getPropertyValue("PERENUAL_API_KEY", "config.properties");
            } catch (Exception e) {
                key = null;
            }
        }
        this.apiKey = key;
    }

    /**
     * Search plant species by keyword.
     *
     * @param query Search query (e.g. plant name).
     * @return List of matching species, or empty list if key missing, query empty, or on error.
     */
    public List<PerenualSpeciesDTO> search(String query) {
        if (apiKey == null || apiKey.isBlank()) {
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
                return Collections.emptyList();
            }
            PerenualSearchResponse parsed = objectMapper.readValue(response.body(), PerenualSearchResponse.class);
            return parsed.getData() != null ? parsed.getData() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
