package dat.security.token;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dat.security.dto.AuthUserDTO;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT token creation and verification using HMAC SHA-256.
 */
public class TokenSecurity implements ITokenSecurity {

    @Override
    public String createToken(AuthUserDTO user, String issuer, String tokenExpireTime, String secretKey) throws Exception {
        long expireMs = Long.parseLong(tokenExpireTime);
        Date expiration = new Date(System.currentTimeMillis() + expireMs);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer(issuer)
                .claim("roles", user.getRoles() != null ? user.getRoles() : Set.of())
                .expirationTime(expiration)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        byte[] secretBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        signedJWT.sign(new MACSigner(secretBytes));

        return signedJWT.serialize();
    }

    @Override
    public boolean tokenIsValid(String token, String secret) throws Exception {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
            return signedJWT.verify(new MACVerifier(secretBytes));
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    @Override
    public boolean tokenNotExpired(String token) throws Exception {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            return expiration != null && expiration.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public AuthUserDTO getUserWithRolesFromToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        String email = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> rolesList = (List<String>) claims.getClaim("roles");
        Set<String> roles = rolesList != null
                ? rolesList.stream().collect(Collectors.toSet())
                : Set.of();
        return new AuthUserDTO(email, roles);
    }
}
