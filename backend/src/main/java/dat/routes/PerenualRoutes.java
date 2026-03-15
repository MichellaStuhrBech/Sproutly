package dat.routes;

import dat.controllers.PerenualController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;

public class PerenualRoutes {

    private final PerenualController perenualController = new PerenualController();

    protected EndpointGroup getRoutes() {
        return () -> {
            get("/search", perenualController::search, Role.USER);
            get("/species/{id}", perenualController::getSpeciesDetails, Role.USER);
        };
    }
}
