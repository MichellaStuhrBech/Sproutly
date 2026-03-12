package dat.controllers;

import dat.daos.impl.AdminNotificationDAO;
import dat.dtos.AdminNotificationDTO;
import dat.entities.AdminNotification;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User-facing controller: returns admin notifications that are due today.
 */
public class NotificationController {

    private final AdminNotificationDAO adminNotificationDAO;

    public NotificationController(EntityManagerFactory emf) {
        this.adminNotificationDAO = new AdminNotificationDAO(emf);
    }

    /**
     * GET /api/notifications/active — returns notifications whose showDate is today.
     */
    public void getActive(Context ctx) {
        LocalDate today = LocalDate.now();
        List<AdminNotificationDTO> list = adminNotificationDAO.findByShowDate(today).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        ctx.json(list);
    }

    private AdminNotificationDTO toDTO(AdminNotification n) {
        return new AdminNotificationDTO(n.getId(), n.getMessage(), n.getShowDate().toString());
    }
}
