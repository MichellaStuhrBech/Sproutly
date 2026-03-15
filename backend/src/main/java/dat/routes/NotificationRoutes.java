package dat.routes;

import dat.controllers.NotificationController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;

public class NotificationRoutes {

    private final NotificationController notificationController;

    public NotificationRoutes(EntityManagerFactory emf) {
        this.notificationController = new NotificationController(emf);
    }

    protected EndpointGroup getRoutes() {
        return () -> get("/active", notificationController::getActive, Role.USER);
    }
}
