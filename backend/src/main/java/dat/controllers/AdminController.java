package dat.controllers;

import dat.daos.impl.AdminNotificationDAO;
import dat.daos.impl.PlantDAO;
import dat.daos.impl.TaskDAO;
import dat.dtos.AdminNotificationDTO;
import dat.dtos.AdminStatsDTO;
import dat.dtos.AdminTaskDTO;
import dat.dtos.TopPlantDTO;
import dat.entities.AdminNotification;
import dat.security.daos.SecurityDAO;
import dat.security.exceptions.ApiException;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminController {

    private final PlantDAO plantDAO;
    private final TaskDAO taskDAO;
    private final SecurityDAO securityDAO;
    private final AdminNotificationDAO adminNotificationDAO;

    public AdminController(EntityManagerFactory emf) {
        this.plantDAO = new PlantDAO(emf);
        this.taskDAO = TaskDAO.getInstance(emf);
        this.securityDAO = new SecurityDAO(emf);
        this.adminNotificationDAO = new AdminNotificationDAO(emf);
    }

    public void getStats(Context ctx) {
        // Access control is already enforced by route role (ADMIN)

        long userCount = securityDAO.getAllUsers().size();

        // Top 10 most picked plants across all users
        List<TopPlantDTO> topPlants = plantDAO.findTopPickedPlants(10).stream()
                .map(arr -> new TopPlantDTO(
                        (String) arr[0],
                        arr[1] instanceof Long l ? l : ((Number) arr[1]).longValue()
                ))
                .collect(Collectors.toList());

        // Last 20 tasks across all users
        List<AdminTaskDTO> lastTasks = taskDAO.findLastTasks(20).stream()
                .map(AdminTaskDTO::new)
                .collect(Collectors.toList());

        ctx.json(new AdminStatsDTO(userCount, topPlants, lastTasks));
    }

    public void createNotification(Context ctx) {
        AdminNotificationDTO dto = ctx.bodyAsClass(AdminNotificationDTO.class);
        String message = dto.getMessage();
        if (message == null || message.isBlank()) {
            throw new ApiException(400, "Message is required");
        }
        LocalDate showDate = parseShowDate(dto.getShowDate());
        AdminNotification n = adminNotificationDAO.create(new AdminNotification(message.trim(), showDate));
        ctx.status(201).json(toDTO(n));
    }

    public void getNotifications(Context ctx) {
        List<AdminNotificationDTO> list = adminNotificationDAO.findAllOrderByShowDateDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        ctx.json(list);
    }

    public void deleteNotification(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        adminNotificationDAO.delete(id);
        ctx.status(204);
    }

    private AdminNotificationDTO toDTO(AdminNotification n) {
        return new AdminNotificationDTO(
                n.getId(),
                n.getMessage(),
                n.getShowDate().toString()
        );
    }

    private static LocalDate parseShowDate(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(400, "showDate is required (YYYY-MM-DD)");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ApiException(400, "showDate must be YYYY-MM-DD");
        }
    }
}

