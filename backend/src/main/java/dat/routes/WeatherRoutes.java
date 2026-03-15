package dat.routes;

import dat.controllers.WeatherController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.get;

public class WeatherRoutes {

    private final WeatherController weatherController = new WeatherController();

    protected EndpointGroup getRoutes() {
        return () -> get("/frost-warning", weatherController::getFrostWarning, Role.USER);
    }
}
