package dat.security.daos;

import dat.dtos.AuthUserDTO;
import dat.security.entities.User;
import dat.security.exceptions.ValidationException;

public interface ISecurityDAO {
    AuthUserDTO getVerifiedUser(String email, String password) throws ValidationException;
    User createUser(String email, String password);
    User addRole(AuthUserDTO user, String newRole);
}
