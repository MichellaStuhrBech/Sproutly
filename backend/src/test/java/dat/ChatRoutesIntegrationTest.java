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
class ChatRoutesIntegrationTest {

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
            em.createQuery("DELETE FROM GardenBed").executeUpdate();
            em.createNativeQuery("DELETE FROM user_roles").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // Do not close emf: shared static from HibernateConfig.getEntityManagerFactoryForTest().

    @Test
    void chat_withoutToken_returns401() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {
            Map<String, String> body = Map.of("message", "What is a tomato?");
            var res = client.post("/api/chat", body);
            assertEquals(401, res.code(), "Expected 401 when calling chat without token");
        });
    }

    @Test
    void chat_withValidToken_butMissingMessage_returns400() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {
            Map<String, String> registerBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );
            var registerRes = client.post("/api/users/register", registerBody);
            assertEquals(201, registerRes.code());

            var loginRes = client.post("/api/auth/login", registerBody);
            assertEquals(200, loginRes.code());
            String token = extractToken(loginRes.body().string());
            assertNotNull(token);

            // Body without "message" or with null message -> controller returns 400
            Map<String, String> noMessage = Map.of();
            var chatRes = client.post("/api/chat", noMessage, req ->
                    req.header("Authorization", "Bearer " + token));

            assertEquals(400, chatRes.code(), "Expected 400 when message is missing");
        });
    }

    @Test
    void chat_withValidToken_andMessage_returns200AndReply() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {
            Map<String, String> registerBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );
            var registerRes = client.post("/api/users/register", registerBody);
            assertEquals(201, registerRes.code());

            var loginRes = client.post("/api/auth/login", registerBody);
            assertEquals(200, loginRes.code());
            String token = extractToken(loginRes.body().string());
            assertNotNull(token);

            Map<String, String> chatBody = Map.of("message", "What is a tomato?");
            var chatRes = client.post("/api/chat", chatBody, req ->
                    req.header("Authorization", "Bearer " + token));

            assertEquals(200, chatRes.code(), "Expected 200 OK for chat with valid token");
            String responseBody = chatRes.body().string();
            assertTrue(responseBody.contains("reply"), "Response should contain a reply field");
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
