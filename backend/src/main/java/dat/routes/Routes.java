package dat.routes;

import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.path;


public class Routes {

    EntityManagerFactory emf;
    private final TodoListRoutes todoListRoutes = new TodoListRoutes();
    private final SowingListRoutes sowingListRoutes = new SowingListRoutes(emf);

    public EndpointGroup getRoutes() {
        return () -> {
            path("/tasks", todoListRoutes.getRoutes());
            path("/sowinglist", sowingListRoutes.getRoutes());
        };
    }

}
