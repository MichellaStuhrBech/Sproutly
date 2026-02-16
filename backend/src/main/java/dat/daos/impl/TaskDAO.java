package dat.daos.impl;

import dat.entities.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskDAO {

    private static TaskDAO instance;
    private static EntityManagerFactory emf;
    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

    public static TaskDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new TaskDAO();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Task create(Task task) {
        try (EntityManager em = getEntityManager()) {
            em.getTransaction().begin();
            em.persist(task);
            em.getTransaction().commit();
            return task;
        } catch (Exception e) {
            logger.error("Create task failed", e);
            throw new dat.security.exceptions.ApiException(400, e.getMessage());
        }
    }
}
