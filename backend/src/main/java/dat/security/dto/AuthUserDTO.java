package dat.security.dto;

import java.util.Set;

/**
 * Authenticated user in context (ctx.attribute("user")).
 * Holds email and roles for authorization.
 */
public class AuthUserDTO {

    private String email;
    private Set<String> roles;
    /** Shown in UI; may be null for legacy accounts. */
    private String displayName;

    public AuthUserDTO() {
    }

    public AuthUserDTO(String email, Set<String> roles) {
        this.email = email;
        this.roles = roles;
    }

    public AuthUserDTO(String email, Set<String> roles, String displayName) {
        this.email = email;
        this.roles = roles;
        this.displayName = displayName;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
