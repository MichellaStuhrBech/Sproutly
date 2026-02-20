package dat.daos.impl;

import dat.config.HibernateConfig;
import dat.entities.SowingPlan;
import dat.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

public class SowingPlanDAO {

        private final EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactory();

        public SowingPlan findById(Long id) {
            try (EntityManager em = emf.createEntityManager()) {
                return em.find(SowingPlan.class, id);
            }
        }

        public SowingPlan findByUser(User user) {
            try (EntityManager em = emf.createEntityManager()) {
                return em.createQuery(
                                "select sp from SowingPlan sp where sp.user.id = :uid",
                                SowingPlan.class)
                        .setParameter("uid", user.getId())
                        .getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }

        public SowingPlan findOrCreateByUser(User user) {

            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();

                SowingPlan plan;
                try {
                    plan = em.createQuery(
                                    "select sp from SowingPlan sp where sp.user.id = :uid",
                                    SowingPlan.class)
                            .setParameter("uid", user.getId())
                            .getSingleResult();

                    em.getTransaction().commit();
                    return plan;

                } catch (NoResultException e) {
                    User managedUser = em.find(User.class, user.getId());

                    plan = new SowingPlan();
                    plan.setUser(managedUser);

                    em.persist(plan);
                    em.getTransaction().commit();
                    return plan;
                }

            } catch (Exception e) {
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
                throw e;
            } finally {
                em.close();
            }
        }

}
