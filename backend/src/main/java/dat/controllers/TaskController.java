package dat.controllers;

import dat.dtos.TaskDTO;
import dat.entities.Task;
import dat.security.entities.User;
import io.javalin.http.Context;

public class TaskController {

    public void create(Context ctx) {
        String userEmail = ctx.attribute("email"); // eller det din security sætter
        TaskDTO dto = ctx.bodyAsClass(TaskDTO.class);

        User user = userDAO.findByEmail(userEmail);

        Task task = new Task(dto.getTitle(), dto.getNotes(), user);
        Task created = taskDAO.create(task);

        ctx.status(201).json(new TaskDTO(created));
    }


}
