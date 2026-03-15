package dat.routes;

import dat.config.HibernateConfig;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.path;


public class Routes {

    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final TodoListRoutes todoListRoutes = new TodoListRoutes();
    private final SowingListRoutes sowingListRoutes = new SowingListRoutes(emf);
    private final ChatRoutes chatRoutes = new ChatRoutes(emf);
    private final AdminRoutes adminRoutes = new AdminRoutes(emf);
    private final WeatherRoutes weatherRoutes = new WeatherRoutes();
    private final NotificationRoutes notificationRoutes = new NotificationRoutes(emf);
    private final PerenualRoutes perenualRoutes = new PerenualRoutes();
    private final GardenBedRoutes gardenBedRoutes = new GardenBedRoutes();

    public EndpointGroup getRoutes() {
        return () -> {
            path("/tasks", todoListRoutes.getRoutes());
            path("/garden-beds", gardenBedRoutes.getRoutes());
            path("/sowinglist", sowingListRoutes.getRoutes());
            path("/chat", chatRoutes.getRoutes());
            path("/admin", adminRoutes.getRoutes());
            path("/weather", weatherRoutes.getRoutes());
            path("/notifications", notificationRoutes.getRoutes());
            path("/plants", perenualRoutes.getRoutes());
        };
    }

}
