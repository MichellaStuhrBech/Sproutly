package dat;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.daos.impl.UserDAO;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegisterUserIntegrationTest {

    private EntityManagerFactory emf;
    private UserDAO userDAO;
    private Javalin app;

    @BeforeAll
    public void init() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = UserDAO.getInstance(emf);
        app = ApplicationConfig.createApp();
    }

    @BeforeEach
    public void setUp() {
        // Clean database before each test
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("DELETE FROM user_roles").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterAll
    void tearDownAll() {
        if (emf != null) emf.close();
    }

    @Test
    void registerUser_validInput_createsUser() {
        JavalinTest.test(app, (server, client) -> {
            Map<String, String> body = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );

            var res = client.post("/api/users/register", body);

            assertEquals(201, res.code(), "Expected 201 Created when registering with valid email and password");

            // DB assert: user is persisted, email stored as unique (username is PK and holds email)
            var em = emf.createEntityManager();
            try {
                var count = em.createQuery(
                                "SELECT COUNT(u) FROM User u WHERE u.username = :email", Long.class)
                        .setParameter("email", "test@sproutly.dk")
                        .getSingleResult();
                assertEquals(1L, count, "Exactly one user with this email should exist in database");
            } finally {
                em.close();
            }
        });
    }

    @Test
    void registerUser_duplicateEmail_returnsConflict() {
        JavalinTest.test(app, (server, client) -> {

            Map<String, String> body = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );

            // First registration should succeed
            var res1 = client.post("/api/users/register", body);
            assertEquals(201, res1.code(), "First registration should succeed");

            // Second registration with same email should fail
            var res2 = client.post("/api/users/register", body);
            assertEquals(409, res2.code(), "Expected 409 Conflict when registering with duplicate email");

            // DB assert: still only 1 user
            var em = emf.createEntityManager();
            try {
                var count = em.createQuery(
                                "SELECT COUNT(u) FROM User u WHERE u.username = :email", Long.class)
                        .setParameter("email", "test@sproutly.dk")
                        .getSingleResult();
                assertEquals(1L, count, "There should still only be one user with this email");
            } finally {
                em.close();
            }
        });
    }


    @Test
    void login_withRegisteredCredentials_returnsOk() {
    }
}
