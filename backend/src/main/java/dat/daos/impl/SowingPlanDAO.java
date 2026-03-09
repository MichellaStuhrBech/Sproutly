package dat.daos.impl;

import dat.config.HibernateConfig;
import dat.entities.SowingPlan;
import dat.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

public class SowingPlanDAO {

    private final EntityManagerFactory emf;

    public SowingPlanDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

        public SowingPlan findById(Long id) {
            try (EntityManager em = emf.createEntityManager()) {
                return em.find(SowingPlan.class, id);
            }
        }

        public SowingPlan findByUser(User user) {
            try (EntityManager em = emf.createEntityManager()) {
                return em.createQuery(
                                "select sp from SowingPlan sp where sp.user.email = :email",
                                SowingPlan.class)
                        .setParameter("email", user.getEmail())
                        .getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }

    public SowingPlan findOrCreateByUser(User user) {
        try (EntityManager em = emf.createEntityManager()) {

            // READ: ingen transaction
            try {
                return em.createQuery(
                                "select sp from SowingPlan sp where sp.user.email = :email",
                                SowingPlan.class)
                        .setParameter("email", user.getEmail())
                        .getSingleResult();

            } catch (NoResultException e) {

                // WRITE: transaction her
                em.getTransaction().begin();

                User managedUser = em.find(User.class, user.getEmail());

                SowingPlan plan = new SowingPlan();
                plan.setUser(managedUser);

                em.persist(plan);
                em.getTransaction().commit();

                return plan;
            }
        }
    }
}
