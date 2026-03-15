package dat.security.daos;


import dat.security.dto.AuthUserDTO;
import dat.security.entities.Role;
import dat.security.entities.User;
import dat.security.exceptions.ApiException;
import dat.security.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.stream.Collectors;


public class SecurityDAO implements ISecurityDAO {

    private static ISecurityDAO instance;
    private static EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory _emf) {
        emf = _emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public AuthUserDTO getVerifiedUser(String email, String password) throws ValidationException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + email); //RuntimeException
            user.getRoles().size(); // force roles to be fetched from db
            if (!user.verifyPassword(password))
                throw new ValidationException("Wrong password");
            return new AuthUserDTO(user.getEmail(), user.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet()), user.getDisplayName());
        }
    }

    @Override
    public User createUser(String email, String password, String displayName) {
        try (EntityManager em = getEntityManager()) {
            User userEntity = em.find(User.class, email);
            if (userEntity != null)
                throw new dat.security.exceptions.ApiException(
                        409,
                        "User with email: " + email + " already exists");
            userEntity = new User(email, password, displayName);
            em.getTransaction().begin();
            Role userRole = em.find(Role.class, "USER");
            if (userRole == null)
                userRole = new Role("USER");
            em.persist(userRole);
            userEntity.addRole(userRole);
            em.persist(userEntity);
            em.getTransaction().commit();
            return userEntity;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(400, e.getMessage());
        }
    }

    @Override
    public User addRole(AuthUserDTO authUser, String newRole) {
        String roleName = newRole != null ? newRole.trim().toUpperCase() : "";
        if (roleName.isEmpty())
            throw new ApiException(400, "Role name is required");
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, authUser.getEmail());
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + authUser.getEmail());
            em.getTransaction().begin();
            Role role = em.find(Role.class, roleName);
            if (role == null) {
                role = new Role(roleName);
                em.persist(role);
            }
            user.addRole(role);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public List<AuthUserDTO> getAllUsers() {
        try (EntityManager em = getEntityManager()) {
            List<User> users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
            return users.stream()
                    .map(u -> new AuthUserDTO(u.getEmail(), u.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet()), u.getDisplayName()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public AuthUserDTO getByEmail(String email) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + email);
            user.getRoles().size();
            return new AuthUserDTO(user.getEmail(), user.getRoles().stream()
                    .map(r -> r.getRoleName() != null ? r.getRoleName().trim().toUpperCase() : "")
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet()), user.getDisplayName());
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + email);
            return user;
        }
    }

    @Override
    public void updateUser(String email, String newPassword) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + email);
            em.getTransaction().begin();
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            em.getTransaction().commit();
        }
    }

    @Override
    public void updateDisplayName(String email, String displayName) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + email);
            em.getTransaction().begin();
            user.setDisplayName(displayName != null && !displayName.isBlank() ? displayName.trim() : null);
            em.getTransaction().commit();
        }
    }

    @Override
    public void deleteUser(String email) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + email);
            em.getTransaction().begin();
            em.remove(user);
            em.getTransaction().commit();
        }
    }

    public User findByEmail(String email) {
        try (EntityManager em = getEntityManager()) {

            List<User> result = em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email",
                            User.class
                    )
                    .setParameter("email", email)
                    .getResultList();

            if (result.isEmpty()) {
                throw new EntityNotFoundException("No user found with email: " + email);
            }

            return result.get(0);
        }
    }
}
