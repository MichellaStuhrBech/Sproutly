package dat.controllers;

import dat.config.HibernateConfig;
import dat.daos.impl.TaskDAO;
import dat.dtos.AuthUserDTO;
import dat.dtos.TaskDTO;
import dat.entities.Task;
import dat.security.daos.SecurityDAO;
import dat.security.entities.User;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;

public class TaskController {

    private final SecurityDAO securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
    private final TaskDAO taskDAO = TaskDAO.getInstance(HibernateConfig.getEntityManagerFactory());

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
            ctx.status(404).json("{\"msg\": \"User not found\"");
        }
    }
}
