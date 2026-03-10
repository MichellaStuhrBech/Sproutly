package dat.security.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import dat.config.HibernateConfig;
import dat.security.dto.AuthUserDTO;
import dat.security.daos.ISecurityDAO;
import dat.security.daos.SecurityDAO;
import dat.security.dto.UserDTO;
import dat.security.entities.User;
import dat.security.exceptions.ApiException;
import dat.security.exceptions.NotAuthorizedException;
import dat.security.exceptions.ValidationException;
import dat.security.token.ITokenSecurity;
import dat.security.token.TokenSecurity;
import dat.utils.Utils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purpose: To handle security in the API
 */
public class SecurityController implements ISecurityController {
    ObjectMapper objectMapper = new ObjectMapper();
    ITokenSecurity tokenSecurity = new TokenSecurity();
    private static ISecurityDAO securityDAO;
    private static SecurityController instance;
    private static Logger logger = LoggerFactory.getLogger(SecurityController.class);

    private SecurityController() { }

    public static SecurityController getInstance() { // Singleton because we don't want multiple instances of the same class
        if (instance == null) {
            instance = new SecurityController();
        }
        securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        return instance;
    }

    @Override
    public Handler login() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode(); // for sending json messages back to the client
            try {
                UserDTO credentials = ctx.bodyAsClass(UserDTO.class);
                AuthUserDTO verifiedUser = securityDAO.getVerifiedUser(credentials.getEmail(), credentials.getPassword());
                String token = createToken(verifiedUser);
                ArrayNode rolesArray = objectMapper.createArrayNode();
                if (verifiedUser.getRoles() != null) {
                    verifiedUser.getRoles().forEach(rolesArray::add);
                }
                ctx.status(200).json(returnObject
                        .put("token", token)
                        .put("email", verifiedUser.getEmail())
                        .set("roles", rolesArray));

            } catch (EntityNotFoundException | ValidationException e) {
                ctx.status(401);
                System.out.println(e.getMessage());
                ctx.json(returnObject.put("msg", e.getMessage()));
            }
        };
    }

    @Override
    public Handler register() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                dat.security.dto.UserDTO userInput = ctx.bodyAsClass(dat.security.dto.UserDTO.class);

                User created = securityDAO.createUser(userInput.getEmail(), userInput.getPassword());

                String token = createToken(new AuthUserDTO(created.getEmail(), Set.of("USER")));
                ArrayNode rolesArray = objectMapper.createArrayNode();
                rolesArray.add("USER");
                ctx.status(HttpStatus.CREATED).json(returnObject
                        .put("token", token)
                        .put("email", created.getEmail())
                        .set("roles", rolesArray));

            } catch (EntityExistsException e) {
                throw new dat.security.exceptions.ApiException(409, "User already exists");
            }
        };
    }

    @Override
    public Handler authenticate() throws UnauthorizedResponse {

        ObjectNode returnObject = objectMapper.createObjectNode();
        return (ctx) -> {
            // This is a preflight request => OK
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }
            String header = ctx.header("Authorization");
            if (header == null) {
                throw new UnauthorizedResponse("Authorization header missing");
            }

            String[] headerParts = header.split(" ");
            if (headerParts.length != 2) {
                throw new UnauthorizedResponse("Authorization header malformed");
            }

            String token = headerParts[1];
            AuthUserDTO verifiedTokenUser = verifyToken(token);

            if (verifiedTokenUser == null) {
                throw new UnauthorizedResponse("Invalid User or Token");
            }
            logger.info("User verified: " + verifiedTokenUser);
            ctx.attribute("user", verifiedTokenUser);
        };
    }

    @Override
    // Check if the user's roles contain any of the allowed roles
    public boolean authorize(AuthUserDTO user, Set<RouteRole> allowedRoles) {
        if (user == null) {
            throw new UnauthorizedResponse("You need to log in");
        }
        Set<String> roleNames = allowedRoles.stream()
                   .map(RouteRole::toString)  // Convert RouteRoles to  Set of Strings
                   .collect(Collectors.toSet());
        Set<String> userRoles = user.getRoles();
        return userRoles != null && userRoles.stream()
                   .map(String::toUpperCase)
                   .anyMatch(roleNames::contains);
        }

    @Override
    public String createToken(AuthUserDTO user) {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(500, "Could not create token");
        }
    }

    @Override
    public AuthUserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            if (SECRET == null || SECRET.isBlank()) {
                logger.warn("SECRET_KEY is null or blank; cannot verify token");
                throw new NotAuthorizedException(403, "Token is not valid");
            }
            if (!tokenSecurity.tokenIsValid(token, SECRET)) {
                logger.warn("Token signature invalid (SECRET_KEY mismatch or tampered token - try logging in again)");
                throw new NotAuthorizedException(403, "Token is not valid");
            }
            if (!tokenSecurity.tokenNotExpired(token)) {
                logger.warn("Token expired (user must log in again)");
                throw new NotAuthorizedException(403, "Token is not valid");
            }
            AuthUserDTO fromToken = tokenSecurity.getUserWithRolesFromToken(token);
            if (fromToken == null || fromToken.getEmail() == null) {
                logger.warn("Token payload missing email or user");
                throw new NotAuthorizedException(403, "Invalid token payload");
            }
            // Use current roles from database so role changes (e.g. adding ADMIN) take effect without re-login
            AuthUserDTO user = securityDAO.getByEmail(fromToken.getEmail());
            logger.info("Token verified for {} with roles: {}", user.getEmail(), user.getRoles());
            return user;
        } catch (EntityNotFoundException e) {
            logger.warn("User not found for token: {}", e.getMessage());
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "User no longer exists");
        } catch (ParseException | JOSEException | NotAuthorizedException e) {
            logger.warn("Token verification failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during token verification", e);
            throw new RuntimeException(e);
        }
    }

    public @NotNull Handler addRole() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // get the role from the body. the json is {"role": "manager"}.
                // We need to get the role from the body and the email from the token
                String newRole = ctx.bodyAsClass(ObjectNode.class).get("role").asText();
                AuthUserDTO user = ctx.attribute("user");
                User updatedUser = securityDAO.addRole(user, newRole);
                ctx.status(200).json(returnObject.put("msg", "Role " + newRole + " added to user"));
            } catch (EntityNotFoundException e) {
                ctx.status(404).json("{\"msg\": \"User not found\"}");
            }
        };
    }

    // Health check for the API. Used in deployment
    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }
}
