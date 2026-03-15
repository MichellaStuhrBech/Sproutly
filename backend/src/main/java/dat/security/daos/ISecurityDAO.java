package dat.security.daos;

import dat.security.dto.AuthUserDTO;
import dat.security.entities.User;
import dat.security.exceptions.ValidationException;

import java.util.List;

public interface ISecurityDAO {
    AuthUserDTO getVerifiedUser(String email, String password) throws ValidationException;
    User createUser(String email, String password, String displayName);
    User addRole(AuthUserDTO user, String newRole);
    List<AuthUserDTO> getAllUsers();
    AuthUserDTO getByEmail(String email);
    User getUserByEmail(String email);
    void updateUser(String email, String newPassword);
    void updateDisplayName(String email, String displayName);
    void deleteUser(String email);
}
