package dat;
import dat.config.HibernateConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import dat.config.ApplicationConfig;
import io.javalin.testtools.JavalinTest;
import java.util.Map;



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddToToDoListIntegrationTest {

    private EntityManagerFactory emf;

    @BeforeAll
    public void init() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
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
    void addTask_withValidToken_returns201AndTaskIsCreated() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {

            // Register
            Map<String, String> registerBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );
            var registerRes = client.post("/api/users/register", registerBody);
            assertEquals(201, registerRes.code());

            // Login
            var loginRes = client.post("/api/auth/login", registerBody);
            assertEquals(200, loginRes.code());

            String token = extractToken(loginRes.body().string());
            assertNotNull(token);

            // Create task
            Map<String, String> taskBody = Map.of(
                    "title", "Water tomatoes",
                    "notes", "Remember fertilizer"
            );

            var taskRes = client.post("/api/tasks", taskBody, req -> {
                req.header("Authorization", "Bearer " + token);
            });

            assertEquals(201, taskRes.code());
            assertTrue(taskRes.body().string().contains("Water tomatoes"));
        });
    }

    private String extractToken(String json) {
        if (json == null) return null;
        int idx = json.indexOf("\"token\"");
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx);
        int firstQuote = json.indexOf("\"", colon + 1);
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (firstQuote == -1 || secondQuote == -1) return null;
        return json.substring(firstQuote + 1, secondQuote);
    }



}
