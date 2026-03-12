package dat.daos.impl;

import dat.entities.AdminNotification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.util.List;

public class AdminNotificationDAO {

    private final EntityManagerFactory emf;

    public AdminNotificationDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public AdminNotification create(AdminNotification n) {
        try (EntityManager em = getEntityManager()) {
            em.getTransaction().begin();
            em.persist(n);
            em.getTransaction().commit();
            return n;
        }
    }

    public List<AdminNotification> findAllOrderByShowDateDesc() {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery(
                    "SELECT n FROM AdminNotification n ORDER BY n.showDate DESC, n.id DESC",
                    AdminNotification.class
            ).getResultList();
        }
    }

    public List<AdminNotification> findByShowDate(LocalDate date) {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery(
                    "SELECT n FROM AdminNotification n WHERE n.showDate = :d ORDER BY n.id",
                    AdminNotification.class
            )
                    .setParameter("d", date)
                    .getResultList();
        }
    }

    public void delete(int id) {
        try (EntityManager em = getEntityManager()) {
            em.getTransaction().begin();
            AdminNotification n = em.find(AdminNotification.class, id);
            if (n != null) {
                em.remove(n);
            }
            em.getTransaction().commit();
        }
    }
}
