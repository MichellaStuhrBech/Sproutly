package dat.controllers;

import dat.config.HibernateConfig;
import dat.daos.impl.TaskDAO;
import dat.security.dto.AuthUserDTO;
import dat.dtos.TaskDTO;
import dat.entities.Task;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;

import dat.security.daos.SecurityDAO;
import dat.security.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class TaskController {

    private final SecurityDAO securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
    private final TaskDAO taskDAO = TaskDAO.getInstance(HibernateConfig.getEntityManagerFactory());

    public void getAll(Context ctx) {
        try {
            AuthUserDTO authUser = ctx.attribute("user");
            if (authUser == null) {
                ctx.status(401).json("{\"msg\": \"You must be logged in to view tasks\"}");
                return;
            }
            List<TaskDTO> dtos = taskDAO.findByUserEmail(authUser.getEmail()).stream()
                    .map(TaskDTO::new)
                    .collect(Collectors.toList());
            ctx.json(dtos);
        } catch (Exception e) {
            ctx.status(500).json("{\"msg\": \"Failed to load tasks\"}");
        }
    }

    public void create(Context ctx) {
        try {
            AuthUserDTO authUser = ctx.attribute("user");
            if (authUser == null) {
                ctx.status(401).json("{\"msg\": \"You must be logged in to create a task\"}");
                return;
            }
            User user = securityDAO.getUserByEmail(authUser.getEmail());
            TaskDTO dto = ctx.bodyAsClass(TaskDTO.class);
            Task task = new Task(dto.getTitle(), dto.getNotes(), user);
            Task created = taskDAO.create(task);
            ctx.status(201).json(new TaskDTO(created));
        } catch (EntityNotFoundException e) {
            ctx.status(404).json("{\"msg\": \"User not found\"}");
        }
    }
}
