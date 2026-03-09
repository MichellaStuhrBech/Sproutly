package dat.daos.impl;
import dat.config.HibernateConfig;
import dat.entities.Plant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

public class PlantDAO {

    private final EntityManagerFactory emf;

    public PlantDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Plant save(Plant plant) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            if (plant.getId() == null) em.persist(plant);
            else plant = em.merge(plant);

            em.getTransaction().commit();
            return plant;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Plant> findBySowingPlanIdSorted(Long sowingPlanId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "select p from Plant p " +
                                    "where p.sowingPlan.id = :spid " +
                                    "order by p.sowingMonth asc, p.name asc",
                            Plant.class)
                    .setParameter("spid", sowingPlanId)
                    .getResultList();
        }
    }

    public List<Plant> findByUserEmailSorted(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "select p from Plant p where p.sowingPlan.user.email = :email " +
                                    "order by p.sowingMonth asc, p.name asc",
                            Plant.class)
                    .setParameter("email", email)
                    .getResultList();
        }
    }

    public Optional<Plant> findByUserEmailAndId(String userEmail, Long plantId) {
        try (EntityManager em = emf.createEntityManager()) {
            Plant p = em.createQuery(
                            "select p from Plant p where p.id = :id and p.sowingPlan.user.email = :email",
                            Plant.class)
                    .setParameter("id", plantId)
                    .setParameter("email", userEmail)
                    .getSingleResult();
            return Optional.of(p);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Plant plant = em.find(Plant.class, id);
            if (plant != null) {
                em.remove(plant);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

}
