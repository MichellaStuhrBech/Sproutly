package dat.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.config.HibernateConfig;
import dat.daos.impl.UserDAO;
import dat.dtos.AuthUserDTO;
import dat.dtos.UserDTO;
import dat.exceptions.ApiException;
import dat.security.entities.User;
import io.javalin.http.Context;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserController {

    private final UserDAO userDAO = UserDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void create(Context ctx) {
        try {
            UserDTO dto = ctx.bodyAsClass(UserDTO.class);
            User user = new User(dto.getEmail(), dto.getPassword());
            User created = userDAO.create(user);
            ctx.status(201).json(objectMapper.createObjectNode().put("email", created.getUsername()));
        } catch (ApiException e) {
            if (e.getStatusCode() == 409) {
                ctx.status(409).json(objectMapper.createObjectNode().put("msg", "User with this email already exists"));
            } else {
                ctx.status(e.getStatusCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
            }
        }
    }

    public void readAll(Context ctx) {
        try {
            List<User> users = userDAO.readAll();
            List<AuthUserDTO> dtos = users.stream()
                    .map(u -> new AuthUserDTO(u.getUsername(), u.getRolesAsStrings() != null ? u.getRolesAsStrings() : Set.of()))
                    .collect(Collectors.toList());
            ctx.json(dtos);
        } catch (ApiException e) {
            ctx.status(e.getStatusCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
        }
    }

    public void read(Context ctx) {
        String email = ctx.pathParam("id");
        try {
            User user = userDAO.read(email);
            AuthUserDTO dto = new AuthUserDTO(user.getUsername(), user.getRolesAsStrings() != null ? user.getRolesAsStrings() : Set.of());
            ctx.json(dto);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                ctx.status(404).json(objectMapper.createObjectNode().put("msg", "User not found"));
            } else {
                ctx.status(e.getStatusCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
            }
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
            User updateData = new User();
            updateData.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
            userDAO.update(email, updateData);
            ctx.status(200).json(objectMapper.createObjectNode().put("msg", "User updated"));
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                ctx.status(404).json(objectMapper.createObjectNode().put("msg", "User not found"));
            } else {
                ctx.status(e.getStatusCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
            }
        }
    }

    public void delete(Context ctx) {
        String email = ctx.pathParam("id");
        try {
            userDAO.delete(email);
            ctx.status(204);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                ctx.status(404).json(objectMapper.createObjectNode().put("msg", "User not found"));
            } else {
                ctx.status(e.getStatusCode()).json(objectMapper.createObjectNode().put("msg", e.getMessage()));
            }
        }
    }
}
