package dat.routes;

import dat.controllers.PlantController;
import dat.controllers.TaskController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SowingListRoutes {

        private final PlantController plantController = new PlantController();

    protected EndpointGroup getRoutes() {
        return () -> {
            get("/", plantController::getAll, Role.USER);
            post("/", plantController::create, Role.USER);
        };
    }

}
