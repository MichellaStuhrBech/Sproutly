package dat.routes;

import dat.controllers.UserController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;

public class CreateUserRoutes {

    private final UserController userController = new UserController();

    protected EndpointGroup getRoutes() {

        return () -> {
            post("/", userController::create, Role.ANYONE);
            get("/", userController::readAll, Role.ANYONE);
            get("/{id}", userController::read, Role.ANYONE);
            put("/{id}", userController::update, Role.ANYONE);
            delete("/{id}", userController::delete, Role.ANYONE);
        };
    }
}
