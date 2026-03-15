package dat.controllers;

import dat.config.HibernateConfig;
import dat.daos.impl.GardenBedDAO;
import dat.dtos.GardenBedDTO;
import dat.entities.GardenBed;
import dat.security.daos.SecurityDAO;
import dat.security.dto.AuthUserDTO;
import dat.security.entities.User;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

public class GardenBedController {

    private final SecurityDAO securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
    private final GardenBedDAO gardenBedDAO = GardenBedDAO.getInstance(HibernateConfig.getEntityManagerFactory());

    public void getAll(Context ctx) {
        try {
            AuthUserDTO authUser = ctx.attribute("user");
            if (authUser == null) {
                ctx.status(401).json("{\"msg\": \"You must be logged in to view garden beds\"}");
                return;
            }
            List<GardenBedDTO> dtos = gardenBedDAO.findByUserEmail(authUser.getEmail()).stream()
                    .map(GardenBedDTO::new)
                    .collect(Collectors.toList());
            ctx.json(dtos);
        } catch (Exception e) {
            ctx.status(500).json("{\"msg\": \"Failed to load garden beds\"}");
        }
    }

    public void create(Context ctx) {
        try {
            AuthUserDTO authUser = ctx.attribute("user");
            if (authUser == null) {
                ctx.status(401).json("{\"msg\": \"You must be logged in to create a garden bed\"}");
                return;
            }
            User user = securityDAO.getUserByEmail(authUser.getEmail());
            GardenBedDTO dto = ctx.bodyAsClass(GardenBedDTO.class);
            GardenBed bed = new GardenBed(
                    dto.getName() != null ? dto.getName() : "",
                    dto.getContents() != null ? dto.getContents() : "",
                    user
            );
            GardenBed created = gardenBedDAO.create(bed);
            ctx.status(201).json(new GardenBedDTO(created));
        } catch (EntityNotFoundException e) {
            ctx.status(404).json("{\"msg\": \"User not found\"}");
        }
    }

    public void update(Context ctx) {
        try {
            AuthUserDTO authUser = ctx.attribute("user");
            if (authUser == null) {
                ctx.status(401).json("{\"msg\": \"You must be logged in to update a garden bed\"}");
                return;
            }
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            GardenBed bed = gardenBedDAO.findById(id);
            if (!bed.getUser().getEmail().equals(authUser.getEmail())) {
                ctx.status(403).json("{\"msg\": \"Not your garden bed\"}");
                return;
            }
            GardenBedDTO dto = ctx.bodyAsClass(GardenBedDTO.class);
            if (dto.getName() != null) bed.setName(dto.getName());
            if (dto.getContents() != null) bed.setContents(dto.getContents());
            GardenBed updated = gardenBedDAO.update(bed);
            ctx.json(new GardenBedDTO(updated));
        } catch (EntityNotFoundException e) {
            ctx.status(404).json("{\"msg\": \"Garden bed not found\"}");
        }
    }

    public void delete(Context ctx) {
        try {
            AuthUserDTO authUser = ctx.attribute("user");
            if (authUser == null) {
                ctx.status(401).json("{\"msg\": \"You must be logged in to delete a garden bed\"}");
                return;
            }
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            GardenBed bed = gardenBedDAO.findById(id);
            if (!bed.getUser().getEmail().equals(authUser.getEmail())) {
                ctx.status(403).json("{\"msg\": \"Not your garden bed\"}");
                return;
            }
            gardenBedDAO.delete(id);
            ctx.status(204);
        } catch (EntityNotFoundException e) {
            ctx.status(404).json("{\"msg\": \"Garden bed not found\"}");
        }
    }
}
