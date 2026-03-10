package dat.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dat.exceptions.ApiException;
import dat.routes.Routes;
import dat.security.controllers.AccessController;
import dat.security.controllers.SecurityController;
import dat.security.daos.SecurityDAO;
import dat.security.dto.AuthUserDTO;
import dat.security.enums.Role;
import dat.security.exceptions.NotAuthorizedException;
import dat.security.routes.SecurityRoutes;
import dat.utils.Utils;
import io.javalin.Javalin;
import jakarta.persistence.EntityNotFoundException;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig {

    private static Routes routes = new Routes();
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static SecurityController securityController = SecurityController.getInstance();
    private static AccessController accessController = new AccessController();
    private static Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static int count = 1;

    public static void configuration(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE);
        config.router.contextPath = "/api"; // base path for all endpoints
        config.router.apiBuilder(routes.getRoutes());
        config.router.apiBuilder(SecurityRoutes.getSecuredRoutes());
        config.router.apiBuilder(SecurityRoutes.getSecurityRoutes());
    }

    /** Creates app with full config and handlers but does not start it. Use for tests. */
    public static Javalin createApp() {
        Javalin app = Javalin.create(ApplicationConfig::configuration);

        app.beforeMatched(accessController::accessHandler);
        app.before(ApplicationConfig::corsHeaders);
        app.options("/*", ApplicationConfig::corsHeadersOptions);

        // Healthcheck endpoint
        app.get("/health", ctx -> {
            ctx.status(200).result("OK");
        });

        app.after(ApplicationConfig::afterRequest);
        app.exception(ApiException.class, ApplicationConfig::apiExceptionHandler);
        app.exception(dat.security.exceptions.ApiException.class, ApplicationConfig::apiSecurityExceptionHandler);
        app.exception(NotAuthorizedException.class, ApplicationConfig::apiNotAuthorizedExceptionHandler);
        app.exception(Exception.class, ApplicationConfig::generalExceptionHandler);
        app.exception(JsonMappingException.class, (e, ctx) -> {
            logger.error("JSON mapping error: {}", e.getMessage());
            ctx.status(400);
            ctx.json(new ApiException(400, "Check JSON fields and values."));
        });

        return app;
    }

    public static Javalin startServer(int port) {
        ensureAdminUserExists();
        Javalin app = createApp();
        app.start(port);
        return app;
    }

    /**
     * Ensures an admin user exists in the database. Uses ADMIN_EMAIL and ADMIN_PASSWORD from
     * config.properties if set; otherwise creates admin@test.dk with password admin123.
     */
    private static void ensureAdminUserExists() {
        String adminEmail = "admin@test.dk";
        String adminPassword = "admin123";
        try {
            String e = Utils.getPropertyValue("ADMIN_EMAIL", "config.properties");
            String p = Utils.getPropertyValue("ADMIN_PASSWORD", "config.properties");
            if (e != null && !e.isBlank()) adminEmail = e.trim();
            if (p != null && !p.isBlank()) adminPassword = p;
        } catch (Exception ignored) {
            // use defaults
        }
        try {
            SecurityDAO securityDAO = new SecurityDAO(dat.config.HibernateConfig.getEntityManagerFactory());
            AuthUserDTO dto = securityDAO.getByEmail(adminEmail);
            if (dto.getRoles() == null || !dto.getRoles().contains("ADMIN")) {
                securityDAO.addRole(dto, "ADMIN");
                logger.info("Added ADMIN role to user: {}", adminEmail);
            }
        } catch (EntityNotFoundException e) {
            try {
                SecurityDAO securityDAO = new SecurityDAO(dat.config.HibernateConfig.getEntityManagerFactory());
                securityDAO.createUser(adminEmail, adminPassword);
                AuthUserDTO dto = securityDAO.getByEmail(adminEmail);
                securityDAO.addRole(dto, "ADMIN");
                logger.info("Created admin user: {} (change password in production)", adminEmail);
            } catch (Exception ex) {
                logger.warn("Could not create admin user: {}", ex.getMessage());
            }
        } catch (Exception e) {
            logger.warn("Could not ensure admin user: {}", e.getMessage());
        }
    }

    public static void afterRequest(Context ctx) {
        String requestInfo = ctx.req().getMethod() + " " + ctx.req().getRequestURI();
        logger.info(" Request {} - {} was handled with status code {}", count++, requestInfo, ctx.status());
    }

    public static void stopServer(Javalin app) {
        app.stop();
    }

    public static void apiExceptionHandler(ApiException e, Context ctx) {
        ctx.status(e.getStatusCode());
        logger.warn("An API exception occurred: Code: {}, Message: {}", e.getStatusCode(), e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    public static void apiSecurityExceptionHandler(dat.security.exceptions.ApiException e, Context ctx) {
        ctx.status(e.getCode());
        logger.warn("A Security API exception occurred: Code: {}, Message: {}", e.getCode(), e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    public static void apiNotAuthorizedExceptionHandler(NotAuthorizedException e, Context ctx) {
        ctx.status(e.getStatusCode());
        logger.warn("A Not authorized Security API exception occurred: Code: {}, Message: {}", e.getStatusCode(), e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    private static void generalExceptionHandler(Exception e, Context ctx) {
        logger.error("An unhandled exception occurred: {}", e.getMessage(), e);
        if (!ctx.res().isCommitted()) {
            ctx.status(500).json(Utils.convertToJsonMessage(ctx, "error", e.getMessage()));
        }
    }

    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    private static void corsHeadersOptions(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
        ctx.status(204);
    }


}