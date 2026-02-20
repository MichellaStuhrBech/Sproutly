package dat.controllers;

import dat.daos.impl.PlantDAO;
import dat.daos.impl.SowingPlanDAO;
import dat.entities.Plant;
import dat.security.entities.User;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class PlantController {

    private PlantDAO plantDAO = new PlantDAO();
    private SowingPlanDAO sowingPlanDAO = new SowingPlanDAO();
    private UserDAO userDAO = new UserDAO();

    public void addPlant(Context ctx) {

        String email = ctx.attribute("email");
        User user = userDAO.findByEmail(email);

        SowingPlan plan = sowingPlanDAO.findByUser(user);

        PlantDTO dto = ctx.bodyAsClass(PlantDTO.class);

        Plant plant = new Plant();
        plant.setName(dto.getName());
        plant.setLatinName(dto.getLatinName());
        plant.setSowingMonth(dto.getSowingMonth());
        plant.setSowingPlan(plan);

        plantDAO.save(plant);

        ctx.status(201).json(plant);
    }

    public void getPlants(Context ctx) {

        String email = ctx.attribute("email");
        User user = userDAO.findByEmail(email);

        SowingPlan plan = sowingPlanDAO.findByUser(user);

        List<Plant> plants = plan.getPlants()
                .stream()
                .sorted(Comparator.comparingInt(Plant::getSowingMonth))
                .toList();

        ctx.json(plants);
    }
}
