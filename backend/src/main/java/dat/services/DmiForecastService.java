package dat.services;

import com.fasterxml.jackson.databind.JsonNode;
import dat.utils.Utils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
/**
 * Calls DMI Forecast Data EDR API to check if temperatures below 0°C are forecast
 * in the next 12–24 hours (can damage or kill plants).
 * @see <a href="https://www.dmi.dk/friedata/dokumentation/forecast-data-edr-api">DMI Forecast Data EDR API</a>
 */
public class DmiForecastService {

    private static final String DMI_EDR_BASE = "https://opendataapi.dmi.dk/v1/forecastedr/collections/harmonie_dini_sf/position";
    private static final double FREEZING_KELVIN = 273.15;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = Utils.getObjectMapper();

    /**
     * Returns true if any forecast in the next 12–24 hours has temperature below 0°C at the given location.
     *
     * @param lat Latitude (e.g. 55.715 for Copenhagen).
     * @param lon Longitude (e.g. 12.561 for Copenhagen).
     * @return true if frost is expected, false otherwise or on API error.
     */
    public boolean hasFrostInNext12To24Hours(double lat, double lon) {
        try {
            Instant now = Instant.now();
            Instant end = now.plus(24, ChronoUnit.HOURS);
            String datetime = now.toString() + "/" + end.toString();
            String coords = "POINT(" + lon + " " + lat + ")";
            String url = DMI_EDR_BASE
                    + "?coords=" + URLEncoder.encode(coords, StandardCharsets.UTF_8)
                    + "&crs=crs84"
                    + "&parameter-name=temperature-2m"
                    + "&f=GeoJSON"
                    + "&datetime=" + URLEncoder.encode(datetime, StandardCharsets.UTF_8);

            var client = java.net.http.HttpClient.newHttpClient();
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                return false;
            }
            return hasFrostInForecastResponse(response.body());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parses DMI GeoJSON forecast response and returns true if any temperature-2m is below 0°C.
     * Visible for testing.
     */
    public boolean hasFrostInForecastResponse(String geoJsonBody) {
        if (geoJsonBody == null || geoJsonBody.isBlank()) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(geoJsonBody);
            JsonNode features = root.path("features");
            if (!features.isArray()) {
                return false;
            }
            for (JsonNode feature : features) {
                JsonNode props = feature.path("properties");
                JsonNode tempNode = props.path("temperature-2m");
                if (tempNode.isNumber()) {
                    double kelvin = tempNode.asDouble();
                    if (kelvin < FREEZING_KELVIN) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
