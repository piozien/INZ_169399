package pl.su.su_backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Getter
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Getter
    @Value("${app.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Value("${app.activation.expiration-ms:10800000}")
    private long activationExpiration;

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public String generateToken(String email) {
        return generateToken(email, null);
    }

    public String generateToken(String email, String fullName) {
        var builder = Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration));

        if (fullName != null && !fullName.isEmpty()) {
            builder.claim("name", fullName);
        }
        builder.claim("email", email);

        return builder
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateActivationToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("typ", "activation")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + activationExpiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean isActivationToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "activation".equals(claims.get("typ"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}