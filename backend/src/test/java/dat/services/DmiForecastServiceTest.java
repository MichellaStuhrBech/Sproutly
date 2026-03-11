package dat.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for frost detection: when forecast has temperature below 0°C, the user should get a warning message.
 */
class DmiForecastServiceTest {

    private final DmiForecastService service = new DmiForecastService();

    private static final String GEOJSON_BELOW_ZERO = """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "geometry": { "type": "Point", "coordinates": [12.55, 55.71] },
              "properties": {
                "temperature-2m": 272.0,
                "step": "2025-03-09T06:00:00.000Z"
              }
            }
          ]
        }
        """;

    private static final String GEOJSON_ABOVE_ZERO = """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "geometry": { "type": "Point", "coordinates": [12.55, 55.71] },
              "properties": {
                "temperature-2m": 275.5,
                "step": "2025-03-09T06:00:00.000Z"
              }
            }
          ]
        }
        """;

    private static final String GEOJSON_MIXED_ONE_BELOW = """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "properties": { "temperature-2m": 276.0 }
            },
            {
              "type": "Feature",
              "properties": { "temperature-2m": 272.8 }
            }
          ]
        }
        """;

    private static final String GEOJSON_EMPTY_FEATURES = """
        {
          "type": "FeatureCollection",
          "features": []
        }
        """;

    @Test
    void hasFrostInForecastResponse_whenTemperatureBelowZero_returnsTrue() {
        assertTrue(service.hasFrostInForecastResponse(GEOJSON_BELOW_ZERO),
            "When forecast has temperature below 0°C (272 K), user should get frost warning message");
    }

    @Test
    void hasFrostInForecastResponse_whenTemperatureAboveZero_returnsFalse() {
        assertFalse(service.hasFrostInForecastResponse(GEOJSON_ABOVE_ZERO),
            "When all temperatures are above 0°C, no frost message");
    }

    @Test
    void hasFrostInForecastResponse_whenOneStepBelowZero_returnsTrue() {
        assertTrue(service.hasFrostInForecastResponse(GEOJSON_MIXED_ONE_BELOW),
            "When any forecast step is below 0°C, user should get frost warning");
    }

    @Test
    void hasFrostInForecastResponse_whenEmptyFeatures_returnsFalse() {
        assertFalse(service.hasFrostInForecastResponse(GEOJSON_EMPTY_FEATURES));
    }

    @Test
    void hasFrostInForecastResponse_whenNullOrBlank_returnsFalse() {
        assertFalse(service.hasFrostInForecastResponse(null));
        assertFalse(service.hasFrostInForecastResponse(""));
        assertFalse(service.hasFrostInForecastResponse("   "));
    }
}
