package dat.routes;

import dat.config.HibernateConfig;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.path;


public class Routes {

    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final TodoListRoutes todoListRoutes = new TodoListRoutes();
    private final SowingListRoutes sowingListRoutes = new SowingListRoutes(emf);

    public EndpointGroup getRoutes() {
        return () -> {
            path("/tasks", todoListRoutes.getRoutes());
            path("/sowinglist", sowingListRoutes.getRoutes());
        };
    }

}
