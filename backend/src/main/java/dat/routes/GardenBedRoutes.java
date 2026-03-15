package dat.routes;

import dat.controllers.GardenBedController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class GardenBedRoutes {

    private final GardenBedController controller = new GardenBedController();

    protected EndpointGroup getRoutes() {
        return () -> {
            get("/", controller::getAll, Role.USER);
            post("/", controller::create, Role.USER);
            put("/{id}", controller::update, Role.USER);
            delete("/{id}", controller::delete, Role.USER);
        };
    }
}
