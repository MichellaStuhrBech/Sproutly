package dat.controllers;

import dat.dtos.FrostWarningDTO;
import dat.services.DmiForecastService;
import io.javalin.http.Context;

/**
 * Weather-related endpoints (e.g. frost warning for gardeners).
 */
public class WeatherController {

    private static final double DEFAULT_LAT = 55.715;
    private static final double DEFAULT_LON = 12.561;
    private static final String FROST_MESSAGE = "Frost is expected in the next 12–24 hours. Temperatures below 0°C can damage or kill plants. Consider protecting them.";

    private final DmiForecastService dmiForecastService = new DmiForecastService();

    /**
     * GET /api/weather/frost-warning?lat=55.715&lon=12.561
     * Returns whether the user should be notified about frost (temp below 0°C) in the next 12–24 hours.
     */
    public void getFrostWarning(Context ctx) {
        double lat = parseDouble(ctx.queryParam("lat"), DEFAULT_LAT);
        double lon = parseDouble(ctx.queryParam("lon"), DEFAULT_LON);

        boolean frostWarning = dmiForecastService.hasFrostInNext12To24Hours(lat, lon);
        String message = frostWarning ? FROST_MESSAGE : null;

        ctx.json(new FrostWarningDTO(frostWarning, message));
    }

    private static double parseDouble(String value, double defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
