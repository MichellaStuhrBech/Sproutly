package dat;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import io.javalin.testtools.JavalinTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.given;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginRoutesIntegrationTest {

    private EntityManagerFactory emf;

    @BeforeAll
    public void init() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
    }

    @BeforeEach
    public void setUp() {
        // Clean database before each test (tasks/plants/sowing plans reference users)
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

    // Do not close emf: shared static from HibernateConfig.getEntityManagerFactoryForTest().

    @Test
    void login_withRegisteredCredentials_returnsOk() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {

            // 1️⃣ Register user first
            Map<String, String> registerBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );

            var registerRes = client.post("/api/users/register", registerBody);
            assertEquals(201, registerRes.code(), "User should be registered");

            // 2️⃣ Login with same credentials
            Map<String, String> loginBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );

            var loginRes = client.post("/api/auth/login", loginBody);

            assertEquals(200, loginRes.code(), "Expected 200 OK on successful login");

            // 3️⃣ Optional: assert token exists in response
            String responseBody = loginRes.body().string();
            assertTrue(responseBody.contains("token"), "Response should contain a token");
        });
    }
    @Test
    void login_wrongPassword_returns401() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {

            // Arrange: register user
            Map<String, String> registerBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );
            var registerRes = client.post("/api/users/register", registerBody);
            assertEquals(201, registerRes.code());

            // Act: login with wrong password
            Map<String, String> loginBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "WrongPassword!"
            );
            var loginRes = client.post("/api/auth/login", loginBody);

            // Assert
            assertEquals(401, loginRes.code(), "Expected 401 Unauthorized with wrong password");
        });
    }

    @Test
    void login_unknownEmail_returns401() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {

            // Act: login with email that doesn't exist
            Map<String, String> loginBody = Map.of(
                    "email", "doesnotexist@sproutly.dk",
                    "password", "Secret123!"
            );
            var loginRes = client.post("/api/auth/login", loginBody);

            // Assert
            assertEquals(401, loginRes.code(), "Expected 401 Unauthorized with unknown email");
        });
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {
            var res = client.get("/api/protected/user_demo");
            assertEquals(401, res.code(), "Expected 401 when calling protected endpoint without token");
        });
    }

    @Test
    void protectedEndpoint_withValidToken_returnsSuccess() {
        JavalinTest.test(ApplicationConfig.createApp(), (server, client) -> {

            // Arrange: register user
            Map<String, String> registerBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );
            var registerRes = client.post("/api/users/register", registerBody);
            assertEquals(201, registerRes.code());

            // Login -> get token
            Map<String, String> loginBody = Map.of(
                    "email", "test@sproutly.dk",
                    "password", "Secret123!"
            );
            var loginRes = client.post("/api/auth/login", loginBody);
            assertEquals(200, loginRes.code());

            String tokenJson = loginRes.body().string();

            String token = extractToken(tokenJson);
            assertNotNull(token, "Token should be extractable from login response");

            // Act: call protected endpoint with token
            var protectedRes = client.get("/api/protected/user_demo", req -> {
                req.header("Authorization", "Bearer " + token);
            });

            assertTrue(
                    protectedRes.code() == 200 || protectedRes.code() == 204,
                    "Expected 200 OK or 204 No Content when calling protected endpoint with valid token"
            );
        });
    }

    // Very small helper that works if response is like: {"token":"..."}
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