
import dat.config.HibernateConfig;
import io.javalin.testtools.JavalinTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import io.javalin.Javalin;
import dat.daos.impl.UserDAO;

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
        }


    @BeforeEach
    public void setUp() {
        // Clean database before each test
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM user").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE vetclinic_id_seq RESTART WITH 1").executeUpdate();
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

            String json = """
                {
                  "email": "test@sproutly.dk",
                  "password": "Secret123!"
                }
                """;

            var res = client.post("/users/register")
                    .body(json)
                    .asString();

            assertEquals(201, res.code());

            // DB assert (JPA)
            var em = emf.createEntityManager();
            try {
                var count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                        .setParameter("email", "test@sproutly.dk")
                        .getSingleResult();
                assertEquals(1L, count);
            } finally {
                em.close();
            }
        });
}


@Test
void registerUser_duplicateEmail_returnsConflict() {  }

@Test
void login_withRegisteredCredentials_returnsOk() {  }
}