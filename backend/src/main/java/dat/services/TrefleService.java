package dat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.dtos.TreflePlantDTO;
import dat.dtos.TrefleSearchResponse;
import dat.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class TrefleService {

    private static final Logger logger = LoggerFactory.getLogger(TrefleService.class);
    private static final String TREFLE_BASE = "https://trefle.io/api/v1/plants/search";
    private final String token;
    private final ObjectMapper objectMapper = Utils.getObjectMapper();

    public TrefleService() {
        this.token = loadTokenFromConfig();
        if (this.token == null || this.token.isBlank()) {
            logger.warn("TREFLE_TOKEN is not set. Set it in the environment, in config.properties, or in a .env file in the project root. Trefle plant search will be empty.");
        }
    }

    private static String loadTokenFromConfig() {
        String key = System.getenv("TREFLE_TOKEN");
        if (key == null || key.isBlank()) {
            try {
                key = Utils.getPropertyValue("TREFLE_TOKEN", "config.properties");
            } catch (Exception e) {
                key = null;
            }
        }
        if (key == null || key.isBlank()) {
            key = Utils.getEnvFromDotEnv("TREFLE_TOKEN");
        }
        return key;
    }

    public List<TreflePlantDTO> search(String query) {
        if (token == null || token.isBlank()) {
            return Collections.emptyList();
        }
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = TREFLE_BASE + "?token=" + token + "&q=" + encoded;
            var client = java.net.http.HttpClient.newHttpClient();
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Collections.emptyList();
            }
            TrefleSearchResponse parsed = objectMapper.readValue(response.body(), TrefleSearchResponse.class);
            return parsed.getData() != null ? parsed.getData() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
