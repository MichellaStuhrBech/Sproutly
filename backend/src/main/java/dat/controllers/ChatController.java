package dat.controllers;

import dat.daos.impl.PlantDAO;
import dat.daos.impl.TaskDAO;
import dat.dtos.ChatRequest;
import dat.dtos.ChatResponse;
import dat.entities.Plant;
import dat.entities.Task;
import dat.security.dto.AuthUserDTO;
import dat.services.PlantChatService;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ChatController {

    private final PlantChatService plantChatService = new PlantChatService();
    private final PlantDAO plantDAO;
    private final TaskDAO taskDAO;

    public ChatController(EntityManagerFactory emf) {
        this.plantDAO = new PlantDAO(emf);
        this.taskDAO = TaskDAO.getInstance(emf);
    }

    public void chat(Context ctx) {
        AuthUserDTO authUser = ctx.attribute("user");
        if (authUser == null) {
            ctx.status(401).json("{\"msg\": \"You must be logged in to use the chat.\"}");
            return;
        }
        ChatRequest req = ctx.bodyAsClass(ChatRequest.class);
        if (req == null || req.getMessage() == null) {
            ctx.status(400).json("{\"msg\": \"Message is required.\"}");
            return;
        }

        String userPlantsSummary = formatUserPlants(plantDAO.findByUserEmailSorted(authUser.getEmail()));
        String userTasksSummary = formatUserTasks(taskDAO.findByUserEmail(authUser.getEmail()));

        String reply = plantChatService.answer(
                req.getMessage(),
                authUser.getEmail(),
                userPlantsSummary,
                userTasksSummary
        );
        ctx.json(new ChatResponse(reply));
    }

    private static String formatUserPlants(List<Plant> plants) {
        if (plants == null || plants.isEmpty()) {
            return "";
        }
        return plants.stream()
                .map(p -> {
                    String name = p.getName() != null ? p.getName() : "";
                    String latin = p.getLatinName() != null && !p.getLatinName().isBlank() ? " (" + p.getLatinName() + ")" : "";
                    int month = p.getSowingMonth();
                    String note = p.getNote() != null && !p.getNote().isBlank() ? ", note: " + p.getNote() : "";
                    String done = p.isCompleted() ? ", completed" : "";
                    return "- " + name + latin + " – sow month: " + month + note + done;
                })
                .collect(Collectors.joining("\n"));
    }

    private static String formatUserTasks(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "";
        }
        return tasks.stream()
                .map(t -> {
                    String title = t.getTitle() != null ? t.getTitle() : "";
                    String notes = t.getNotes() != null && !t.getNotes().isBlank() ? " – " + t.getNotes() : "";
                    return "- " + title + notes;
                })
                .collect(Collectors.joining("\n"));
    }
}
