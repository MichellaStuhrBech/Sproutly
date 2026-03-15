import dat.config.HibernateConfig;
import jakarta.persistence.EntityManagerFactory;
import io.restassured.RestAssured;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

/**
 * Disabled: RestAssured.port was never set (port stayed 0), and API paths may not match
 * actual routes (/api/sowinglist vs /api/sowing-plans). Re-enable after rewriting to use
 * JavalinTest.test() with the provided client, or start the server in @BeforeAll and set port.
 */
@Disabled("Needs server port and route alignment; use JavalinTest or start server in @BeforeAll")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlantSowingIntegrationTest {

        private EntityManagerFactory emf;
        private static int port;
        private String token;
        private int planId;

    @BeforeAll
    public void init() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
    }
        @BeforeAll
        static void beforeAll() {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = port > 0 ? port : 18081;
        }

        @BeforeEach
        void setUp() {
            // 1) reset DB (truncate) eller start ny in-memory/testcontainer
            // 2) create user + login -> token
            token = loginAndGetToken("test@sprout.ly", "test1234");

            // 3) create sowing plan -> planId
            planId = createSowingPlan(token, "My plan");
        }

        @Test
        void userCanAddPlantToSowingPlan() {
            String body = """
        {
          "name": "Tomato",
          "latinName": "Solanum lycopersicum",
          "sowingStartMonth": 3,
          "sowingEndMonth": 5
        }
        """;

            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(body)
                    .when()
                    .post("/api/sowing-plans/{planId}/plants", planId)
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("name", equalTo("Tomato"))
                    .body("latinName", equalTo("Solanum lycopersicum"))
                    .body("sowingStartMonth", equalTo(3))
                    .body("sowingEndMonth", equalTo(5));

            // verify it persists and is linked to this plan
            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/sowing-plans/{planId}/plants", planId)
                    .then()
                    .statusCode(200)
                    .body("size()", equalTo(1))
                    .body("[0].name", equalTo("Tomato"));
        }

        // --- helpers (brug din eksisterende login/create metoder eller lav dem) ---

        private String loginAndGetToken(String email, String password) {
            String loginBody = """
        { "email": "%s", "password": "%s" }
        """.formatted(email, password);

            return given()
                    .contentType("application/json")
                    .body(loginBody)
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("token");
        }

        private int createSowingPlan(String token, String title) {
            String planBody = """
        { "title": "%s" }
        """.formatted(title);

            return given()
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(planBody)
                    .when()
                    .post("/api/sowing-plans")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");
        }


}
