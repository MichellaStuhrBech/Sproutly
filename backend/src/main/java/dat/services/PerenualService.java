package dat.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dat.dtos.PerenualImageDTO;
import dat.dtos.PerenualSearchResponse;
import dat.dtos.PerenualSpeciesDTO;
import dat.dtos.PerenualSpeciesDetailsDTO;
import dat.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    private static final String PERENUAL_SPECIES_DETAILS_V2 = "https://www.perenual.com/api/v2/species/details/";
    private static final String PERENUAL_SPECIES_DETAILS_V1 = "https://www.perenual.com/api/species/details/";

    /**
     * Fetch full details for a species by id. Tries v2 then v1 URL; parses via JsonNode so odd API fields don't break Jackson.
     */
    public PerenualSpeciesDetailsDTO getDetails(int speciesId) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        String body = null;
        try {
            var client = java.net.http.HttpClient.newHttpClient();
            for (String base : new String[]{PERENUAL_SPECIES_DETAILS_V2, PERENUAL_SPECIES_DETAILS_V1}) {
                String url = base + speciesId + "?key=" + apiKey;
                var request = java.net.http.HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() == 200) {
                    body = response.body();
                    break;
                }
                logger.debug("Perenual species details {} returned status {}", url.replace(apiKey, "***"), response.statusCode());
            }
            if (body == null || body.isBlank()) {
                logger.warn("Perenual species details failed for id {} (no 200 from v2 or v1)", speciesId);
                return null;
            }
            JsonNode root = objectMapper.readTree(body);
            if (!root.has("id") && !root.has("common_name")) {
                logger.warn("Perenual species details id {}: unexpected JSON (no id/common_name)", speciesId);
                return null;
            }
            return detailsFromJson(root);
        } catch (Exception e) {
            logger.warn("Perenual getDetails id {}: {}", speciesId, e.getMessage());
            return null;
        }
    }

    private static PerenualSpeciesDetailsDTO detailsFromJson(JsonNode n) {
        PerenualSpeciesDetailsDTO d = new PerenualSpeciesDetailsDTO();
        if (n.has("id") && n.get("id").isNumber()) {
            d.setId(n.get("id").asInt());
        }
        d.setCommonName(text(n, "common_name"));
        d.setScientificName(stringList(n, "scientific_name"));
        d.setOtherName(stringList(n, "other_name"));
        d.setFamily(text(n, "family"));
        d.setOrigin(stringList(n, "origin"));
        d.setType(text(n, "type"));
        d.setCycle(text(n, "cycle"));
        d.setWatering(text(n, "watering"));
        d.setSunlight(stringList(n, "sunlight"));
        d.setSoil(stringList(n, "soil"));
        d.setPruningMonth(stringList(n, "pruning_month"));
        d.setDescription(text(n, "description"));
        d.setGrowthRate(text(n, "growth_rate"));
        d.setMaintenance(text(n, "maintenance"));
        d.setCareLevel(text(n, "care_level"));
        d.setFloweringSeason(text(n, "flowering_season"));
        d.setPoisonousToHumans(bool(n, "poisonous_to_humans"));
        d.setPoisonousToPets(bool(n, "poisonous_to_pets"));
        d.setEdibleFruit(bool(n, "edible_fruit"));
        d.setEdibleLeaf(bool(n, "edible_leaf"));
        d.setIndoor(bool(n, "indoor"));
        d.setFlowers(bool(n, "flowers"));
        d.setMedicinal(bool(n, "medicinal"));
        d.setPestSusceptibility(stringList(n, "pest_susceptibility"));
        if (n.has("hardiness") && n.get("hardiness").isObject()) {
            var h = new HashMap<String, String>();
            n.get("hardiness").fields().forEachRemaining(e -> h.put(e.getKey(), e.getValue().asText("")));
            d.setHardiness(h);
        }
        if (n.has("default_image") && n.get("default_image").isObject()) {
            JsonNode img = n.get("default_image");
            d.setDefaultImage(new PerenualImageDTO(
                    text(img, "thumbnail"),
                    text(img, "small_url"),
                    text(img, "regular_url"),
                    text(img, "medium_url"),
                    text(img, "original_url")
            ));
        }
        return d;
    }

    private static String text(JsonNode n, String field) {
        JsonNode x = n.get(field);
        return x != null && !x.isNull() ? x.asText(null) : null;
    }

    private static Boolean bool(JsonNode n, String field) {
        JsonNode x = n.get(field);
        if (x == null || x.isNull()) return null;
        if (x.isBoolean()) return x.asBoolean();
        return null;
    }

    private static List<String> stringList(JsonNode n, String field) {
        JsonNode x = n.get(field);
        if (x == null || !x.isArray()) return null;
        List<String> out = new ArrayList<>();
        for (JsonNode e : x) {
            if (e.isTextual()) out.add(e.asText());
        }
        return out.isEmpty() ? null : out;
    }
}
