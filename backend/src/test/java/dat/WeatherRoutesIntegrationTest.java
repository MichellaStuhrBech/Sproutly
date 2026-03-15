package dat;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import io.javalin.testtools.JavalinTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WeatherRoutesIntegrationTest {

    private EntityManagerFactory emf;

    @BeforeAll
    public void init() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
    }

    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Task").executeUpdate();
            em.createQuery("DELETE FROM Plant").executeUpdate();
            em.createQuery("DELETE FROM SowingPlan").executeUpdate();
            em.createNativeQuery("DELETE FROM user_roles").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // Do not close emf: it is the shared static from HibernateConfig.getEntityManagerFactoryForTest().
    // Closing it would break other test classes that run after this one.

    @Test
    void frostWarning_withoutToken_returns401() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {
            var res = client.get("/api/weather/frost-warning");
            assertEquals(401, res.code(), "Expected 401 when calling frost-warning without token");
        });
    }

    @Test
    void frostWarning_withValidToken_returns200AndFrostWarningStructure() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {
            var registerBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );
            var registerRes = client.post("/api/users/register", registerBody);
            assertEquals(201, registerRes.code());

            var loginRes = client.post("/api/auth/login", registerBody);
            assertEquals(200, loginRes.code());
            String token = extractToken(loginRes.body().string());
            assertNotNull(token);

            var res = client.get("/api/weather/frost-warning", req ->
                    req.header("Authorization", "Bearer " + token));

            assertEquals(200, res.code(), "Expected 200 OK for frost-warning with valid token");
            String body = res.body().string();
            assertTrue(body.contains("frostWarning"), "Response must contain frostWarning so frontend can show message when below 0°C");
            assertTrue(body.contains("message"), "Response must contain message for the user when frost is expected");
        });
    }

    private static String extractToken(String json) {
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
