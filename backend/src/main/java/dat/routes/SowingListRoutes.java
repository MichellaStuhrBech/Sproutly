package dat.routes;

import dat.controllers.PlantController;
import dat.controllers.TaskController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SowingListRoutes {

    private final PlantController plantController;

    public SowingListRoutes(EntityManagerFactory emf) {
        this.plantController = new PlantController(emf);
    }
    protected EndpointGroup getRoutes() {
        return () -> {
            get("/", plantController::getPlants, Role.USER);
            get("/search", plantController::searchPlants, Role.USER);
            post("/", plantController::addPlant, Role.USER);
            put("/{id}", plantController::updatePlant, Role.USER);
            delete("/{id}", plantController::deletePlant, Role.USER);
        };
    }

}
