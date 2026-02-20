package dat.security.daos;

import dat.dtos.AuthUserDTO;
import dat.security.entities.User;
import dat.security.exceptions.ValidationException;

import java.util.List;

public interface ISecurityDAO {
    AuthUserDTO getVerifiedUser(String email, String password) throws ValidationException;
    User createUser(String email, String password);
    User addRole(AuthUserDTO user, String newRole);
    List<AuthUserDTO> getAllUsers();
    AuthUserDTO getByEmail(String email);
    User getUserByEmail(String email);
    void updateUser(String email, String newPassword);
    void deleteUser(String email);
}
