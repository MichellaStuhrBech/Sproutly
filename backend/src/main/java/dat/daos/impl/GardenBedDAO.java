package dat.daos.impl;

import dat.entities.GardenBed;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GardenBedDAO {

    private static GardenBedDAO instance;
    private static EntityManagerFactory emf;
    private static final Logger logger = LoggerFactory.getLogger(GardenBedDAO.class);

    public static GardenBedDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new GardenBedDAO();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public GardenBed create(GardenBed bed) {
        try (EntityManager em = getEntityManager()) {
            em.getTransaction().begin();
            em.persist(bed);
            em.getTransaction().commit();
            return bed;
        } catch (Exception e) {
            logger.error("Create garden bed failed", e);
            throw new dat.security.exceptions.ApiException(400, e.getMessage());
        }
    }

    public List<GardenBed> findByUserEmail(String email) {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery("SELECT g FROM GardenBed g WHERE g.user.email = :email ORDER BY g.id", GardenBed.class)
                    .setParameter("email", email)
                    .getResultList();
        }
    }

    public GardenBed findById(Long id) {
        try (EntityManager em = getEntityManager()) {
            GardenBed bed = em.find(GardenBed.class, id);
            if (bed == null) {
                throw new EntityNotFoundException("Garden bed not found: " + id);
            }
            return bed;
        }
    }

    public GardenBed update(GardenBed bed) {
        try (EntityManager em = getEntityManager()) {
            em.getTransaction().begin();
            GardenBed merged = em.merge(bed);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            logger.error("Update garden bed failed", e);
            throw new dat.security.exceptions.ApiException(400, e.getMessage());
        }
    }

    public void delete(Long id) {
        try (EntityManager em = getEntityManager()) {
            em.getTransaction().begin();
            GardenBed bed = em.find(GardenBed.class, id);
            if (bed != null) {
                em.remove(bed);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Delete garden bed failed", e);
            throw new dat.security.exceptions.ApiException(400, e.getMessage());
        }
    }
}
