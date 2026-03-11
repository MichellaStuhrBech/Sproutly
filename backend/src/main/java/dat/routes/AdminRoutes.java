package dat.routes;

import dat.controllers.AdminController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;

public class AdminRoutes {

    private final AdminController adminController;

    public AdminRoutes(EntityManagerFactory emf) {
        this.adminController = new AdminController(emf);
    }

    protected EndpointGroup getRoutes() {
        return () -> {
            // GET /api/admin/stats
            get("/stats", adminController::getStats, Role.ADMIN);
        };
    }
}

