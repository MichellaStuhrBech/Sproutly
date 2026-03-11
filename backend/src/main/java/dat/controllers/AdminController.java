package dat.controllers;

import dat.daos.impl.PlantDAO;
import dat.daos.impl.TaskDAO;
import dat.dtos.AdminStatsDTO;
import dat.dtos.AdminTaskDTO;
import dat.dtos.TopPlantDTO;
import dat.security.daos.SecurityDAO;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class AdminController {

    private final PlantDAO plantDAO;
    private final TaskDAO taskDAO;
    private final SecurityDAO securityDAO;

    public AdminController(EntityManagerFactory emf) {
        this.plantDAO = new PlantDAO(emf);
        this.taskDAO = TaskDAO.getInstance(emf);
        this.securityDAO = new SecurityDAO(emf);
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
}

