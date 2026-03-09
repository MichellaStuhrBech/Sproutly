package dat.security.token;

import dat.security.dto.AuthUserDTO;

public interface ITokenSecurity {
    String createToken(AuthUserDTO user, String issuer, String tokenExpireTime, String secretKey) throws Exception;
    boolean tokenIsValid(String token, String secret) throws Exception;
    boolean tokenNotExpired(String token) throws Exception;
    AuthUserDTO getUserWithRolesFromToken(String token) throws Exception;
}
