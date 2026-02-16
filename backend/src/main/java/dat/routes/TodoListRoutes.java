package dat.routes;

import dat.controllers.TaskController;

import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TodoListRoutes {

    private final TaskController taskController = new TaskController();

    protected EndpointGroup getRoutes() {
        return () -> {
            post("/", taskController::create, Role.USER);
        };
    }
}
