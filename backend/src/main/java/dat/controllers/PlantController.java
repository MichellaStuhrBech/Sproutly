package dat.controllers;

import dat.daos.impl.PlantDAO;
import dat.daos.impl.SowingPlanDAO;
import dat.dtos.PlantDTO;
import dat.entities.Plant;
import dat.entities.SowingPlan;
import dat.security.daos.SecurityDAO;
import dat.security.dto.AuthUserDTO;
import dat.security.entities.User;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;


import java.util.Comparator;
import java.util.List;

public class PlantController {



    private PlantDAO plantDAO;
    private SowingPlanDAO sowingPlanDAO;
    private SecurityDAO userDAO;


    public PlantController(EntityManagerFactory emf) {
        this.userDAO = new SecurityDAO(emf);
        this.plantDAO = new PlantDAO(emf);
        this.sowingPlanDAO = new SowingPlanDAO(emf);
    }

    public void addPlant(Context ctx) {

        AuthUserDTO authUser = ctx.attribute("user");
        User user = userDAO.findByEmail(authUser.getEmail());

        SowingPlan plan = sowingPlanDAO.findOrCreateByUser(user);

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
        AuthUserDTO authUser = ctx.attribute("user");
        User user = userDAO.findByEmail(authUser.getEmail());

        SowingPlan plan = sowingPlanDAO.findOrCreateByUser(user);

        List<Plant> plants = plan.getPlants().stream()
                .sorted(Comparator.comparingInt(Plant::getSowingMonth))
                .toList();

        ctx.json(plants);
    }

}
