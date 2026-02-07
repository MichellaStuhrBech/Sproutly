package dat.daos.impl;

import dat.daos.IDAO;
import dat.exceptions.ApiException;
import dat.security.entities.Role;
import dat.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class UserDAO implements IDAO<User, String> {

    private static UserDAO instance;
    private static EntityManagerFactory emf;
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public static UserDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new UserDAO();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public User read(String email) throws ApiException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new ApiException(404, "User not found");
            user.getRoles().size();
            return user;
        }
    }

    @Override
    public List<User> readAll() throws ApiException {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery("SELECT u FROM User u", User.class).getResultList();
        }
    }

    @Override
    public User create(User user) throws ApiException {
        try (EntityManager em = getEntityManager()) {
            if (em.find(User.class, user.getUsername()) != null)
                throw new ApiException(409, "User with this email already exists");
            em.getTransaction().begin();
            Role userRole = em.find(Role.class, "user");
            if (userRole == null) {
                userRole = new Role("user");
                em.persist(userRole);
            }
            user.addRole(userRole);
            em.persist(user);
            em.getTransaction().commit();
            return user;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Create user failed", e);
            throw new ApiException(400, e.getMessage());
        }
    }

    @Override
    public User update(String email, User updated) throws ApiException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new ApiException(404, "User not found");
            em.getTransaction().begin();
            user.setPassword(updated.getPassword() != null ? updated.getPassword() : user.getPassword());
            em.getTransaction().commit();
            return user;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Update user failed", e);
            throw new ApiException(400, e.getMessage());
        }
    }

    @Override
    public void delete(String email) throws ApiException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new ApiException(404, "User not found");
            em.getTransaction().begin();
            em.remove(user);
            em.getTransaction().commit();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Delete user failed", e);
            throw new ApiException(400, e.getMessage());
        }
    }
}
