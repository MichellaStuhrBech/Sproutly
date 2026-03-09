package dat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.dtos.TreflePlantDTO;
import dat.dtos.TrefleSearchResponse;
import dat.utils.Utils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class TrefleService {

    private static final String TREFLE_BASE = "https://trefle.io/api/v1/plants/search";
    private final String token;
    private final ObjectMapper objectMapper = Utils.getObjectMapper();

    public TrefleService() {
        this.token = Utils.getPropertyValue("TREFLE_TOKEN", "config.properties");
    }

    public List<TreflePlantDTO> search(String query) {
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
