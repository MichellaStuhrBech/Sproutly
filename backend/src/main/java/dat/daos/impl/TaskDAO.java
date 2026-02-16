package dat.daos.impl;

import dat.daos.IDAO;
import dat.dtos.TaskDTO;
import dat.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class TaskDAO implements IDAO<TaskDTO, Integer> {

    private static TaskDAO instance;
    private static EntityManagerFactory emf;
    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);


    @Override
    public TaskDTO read(Integer integer) throws ApiException {
        return null;
    }

    @Override
    public List<TaskDTO> readAll() throws ApiException {
        return List.of();
    }

    @Override
    public TaskDTO create(TaskDTO taskDTO) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(taskDTO);
            em.getTransaction().commit();
            return taskDTO;
        }
    }

    @Override
    public TaskDTO update(Integer integer, TaskDTO taskDTO) throws ApiException {
        return null;
    }

    @Override
    public void delete(Integer integer) throws ApiException {

    }

}
