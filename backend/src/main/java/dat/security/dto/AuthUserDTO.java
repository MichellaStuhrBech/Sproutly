package dat.security.dto;

import java.util.Set;

/**
 * Authenticated user in context (ctx.attribute("user")).
 * Holds email and roles for authorization.
 */
public class AuthUserDTO {

    private String email;
    private Set<String> roles;

    public AuthUserDTO() {
    }

    public AuthUserDTO(String email, Set<String> roles) {
        this.email = email;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
