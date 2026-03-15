package dat.security.controllers;

import dat.security.dto.AuthUserDTO;
import dat.security.enums.Role;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Purpose: To handle security in the API at the route level
 */

public class AccessController implements IAccessController {

    private static final Logger logger = LoggerFactory.getLogger(AccessController.class);
    SecurityController securityController = SecurityController.getInstance();

    /**
     * This method checks if the user has the necessary roles to access the route.
     * @param ctx
     */
    public void accessHandler(Context ctx) {

        // If no roles are specified on the endpoint, then anyone can access the route
        if (ctx.routeRoles().isEmpty() || ctx.routeRoles().contains(Role.ANYONE)){
           return;
        }

        // Check if the user is authenticated
        try {
            securityController.authenticate().handle(ctx);
        } catch (UnauthorizedResponse e) {
            throw new UnauthorizedResponse(e.getMessage());
        } catch (Exception e) {
            throw new UnauthorizedResponse("You need to log in! Or you token is invalid.");
        }

        // Check if the user has the necessary roles to access the route
        AuthUserDTO user = ctx.attribute("user");
        Set<RouteRole> allowedRoles = ctx.routeRoles(); // roles allowed for the current route
        if (!securityController.authorize(user, allowedRoles)) {
            logger.warn("Authorization failed: user roles={}, required={}", user != null ? user.getRoles() : "null", allowedRoles);
            throw new UnauthorizedResponse("Unauthorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
        }
    }
}
