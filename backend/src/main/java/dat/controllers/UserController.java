package dat.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.config.HibernateConfig;
import dat.dtos.AuthUserDTO;
import dat.dtos.UserDTO;
import dat.security.daos.SecurityDAO;
import dat.security.exceptions.ApiException;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public class UserController {

    private final SecurityDAO securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void create(Context ctx) {
        try {
            UserDTO dto = ctx.bodyAsClass(UserDTO.class);
            var created = securityDAO.createUser(dto.getEmail(), dto.getPassword());
            ctx.status(201).json(objectMapper.createObjectNode().put("email", created.getEmail()));
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                ctx.status(409).json(objectMapper.createObjectNode().put("msg", "User with this email already exists"));
            } else {
                ctx.status(e.getCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
            }
        }
    }

    public void readAll(Context ctx) {
        try {
            List<AuthUserDTO> users = securityDAO.getAllUsers();
            ctx.json(users);
        } catch (ApiException e) {
            ctx.status(e.getCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
        }
    }

    public void read(Context ctx) {
        String email = ctx.pathParam("id");
        try {
            AuthUserDTO dto = securityDAO.getByEmail(email);
            ctx.json(dto);
        } catch (EntityNotFoundException e) {
            ctx.status(404).json(objectMapper.createObjectNode().put("msg", "User not found"));
        } catch (ApiException e) {
            ctx.status(e.getCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
        }
    }

    public void update(Context ctx) {
        String email = ctx.pathParam("id");
        try {
            UserDTO dto = ctx.bodyAsClass(UserDTO.class);
            if (dto.getPassword() == null || dto.getPassword().isBlank()) {
                ctx.status(400).json(objectMapper.createObjectNode().put("msg", "Password is required"));
                return;
            }
            securityDAO.updateUser(email, dto.getPassword());
            ctx.status(200).json(objectMapper.createObjectNode().put("msg", "User updated"));
        } catch (EntityNotFoundException e) {
            ctx.status(404).json(objectMapper.createObjectNode().put("msg", "User not found"));
        } catch (ApiException e) {
            ctx.status(e.getCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
        }
    }

    public void delete(Context ctx) {
        String email = ctx.pathParam("id");
        try {
            securityDAO.deleteUser(email);
            ctx.status(204);
        } catch (EntityNotFoundException e) {
            ctx.status(404).json(objectMapper.createObjectNode().put("msg", "User not found"));
        } catch (ApiException e) {
            ctx.status(e.getCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
        }
    }
}
