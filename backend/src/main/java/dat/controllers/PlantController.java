package dat.controllers;

import dat.daos.impl.PlantDAO;
import dat.daos.impl.SowingPlanDAO;
import dat.dtos.PlantDTO;
import dat.dtos.TreflePlantDTO;
import dat.entities.Plant;
import dat.entities.SowingPlan;
import dat.security.daos.SecurityDAO;
import dat.security.dto.AuthUserDTO;
import dat.security.entities.User;
import dat.services.TrefleService;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.Comparator;
import java.util.List;

public class PlantController {

    private final PlantDAO plantDAO;
    private final SowingPlanDAO sowingPlanDAO;
    private final SecurityDAO userDAO;
    private final TrefleService trefleService = new TrefleService();

    public PlantController(EntityManagerFactory emf) {
        this.userDAO = new SecurityDAO(emf);
        this.plantDAO = new PlantDAO(emf);
        this.sowingPlanDAO = new SowingPlanDAO(emf);
    }

    public void addPlant(Context ctx) {
        System.out.println("AUTH USER = " + ctx.attribute("user"));

        AuthUserDTO authUser = ctx.attribute("user");
        User user = userDAO.getUserByEmail(authUser.getEmail());

        SowingPlan plan = sowingPlanDAO.findOrCreateByUser(user);

        PlantDTO dto = ctx.bodyAsClass(PlantDTO.class);

        Plant plant = new Plant();
        plant.setName(dto.getName());
        plant.setLatinName(dto.getLatinName());
        plant.setSowingMonth(dto.getSowingMonth());
        plant.setSowingPlan(plan);

        plantDAO.save(plant);

        ctx.status(201).json(new PlantDTO(plant));
    }

    public void getPlants(Context ctx) {
        AuthUserDTO authUser = ctx.attribute("user");
        if (authUser == null) {
            ctx.status(401).json("{\"msg\": \"You must be logged in to view plants\"}");
            return;
        }
        List<PlantDTO> dtos = plantDAO.findByUserEmailSorted(authUser.getEmail()).stream()
                .map(PlantDTO::new)
                .toList();
        ctx.json(dtos);
    }

    public void searchPlants(Context ctx) {
        String q = ctx.queryParam("q");
        List<TreflePlantDTO> suggestions = trefleService.search(q);
        ctx.json(suggestions);
    }

    public void updatePlant(Context ctx) {
        AuthUserDTO authUser = ctx.attribute("user");
        if (authUser == null) {
            ctx.status(401).json("{\"msg\": \"You must be logged in to update a plant\"}");
            return;
        }
        Long plantId = Long.valueOf(ctx.pathParam("id"));
        var opt = plantDAO.findByUserEmailAndId(authUser.getEmail(), plantId);
        if (opt.isEmpty()) {
            ctx.status(404).json("{\"msg\": \"Plant not found\"}");
            return;
        }
        Plant plant = opt.get();
        PlantDTO dto = ctx.bodyAsClass(PlantDTO.class);
        plant.setSowingMonth(dto.getSowingMonth());
        plant.setNote(dto.getNote());
        plant.setCompleted(dto.isCompleted());
        plantDAO.save(plant);
        ctx.json(new PlantDTO(plant));
    }

    public void deletePlant(Context ctx) {
        AuthUserDTO authUser = ctx.attribute("user");
        if (authUser == null) {
            ctx.status(401).json("{\"msg\": \"You must be logged in to delete a plant\"}");
            return;
        }
        Long plantId = Long.valueOf(ctx.pathParam("id"));
        var opt = plantDAO.findByUserEmailAndId(authUser.getEmail(), plantId);
        if (opt.isEmpty()) {
            ctx.status(404).json("{\"msg\": \"Plant not found\"}");
            return;
        }
        plantDAO.delete(plantId);
        ctx.status(204);
    }

}
