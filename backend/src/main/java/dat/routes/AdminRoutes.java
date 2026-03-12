package dat.routes;

import dat.controllers.AdminController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AdminRoutes {

    private final AdminController adminController;

    public AdminRoutes(EntityManagerFactory emf) {
        this.adminController = new AdminController(emf);
    }

    protected EndpointGroup getRoutes() {
        return () -> {
            get("/stats", adminController::getStats, Role.ADMIN);
            post("/notifications", adminController::createNotification, Role.ADMIN);
            get("/notifications", adminController::getNotifications, Role.ADMIN);
            delete("/notifications/{id}", adminController::deleteNotification, Role.ADMIN);
        };
    }
}

