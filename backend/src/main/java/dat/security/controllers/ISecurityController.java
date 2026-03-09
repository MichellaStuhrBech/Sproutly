package dat.security.controllers;

import dat.security.dto.AuthUserDTO;
import io.javalin.http.Handler;
import io.javalin.security.RouteRole;

import java.util.Set;

/**
 * Purpose: To handle security in the API
 */
public interface ISecurityController {
    Handler login(); // to get a token
    Handler register(); // to get a user
    Handler authenticate(); // to verify roles inside token
    boolean authorize(AuthUserDTO userDTO, Set<RouteRole> allowedRoles); // to verify user roles
    String createToken(AuthUserDTO user) throws Exception;
    AuthUserDTO verifyToken(String token) throws Exception;
}
