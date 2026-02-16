package dat.routes;

import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.path;


public class Routes {

    private final CreateUserRoutes createUserRoutes = new CreateUserRoutes();
    private final TodoListRoutes todoListRoutes = new TodoListRoutes();

    public EndpointGroup getRoutes() {
        return () -> {
            path("/createuser", createUserRoutes.getRoutes());
            path("/tasks", todoListRoutes.getRoutes());
        };
    }

}
