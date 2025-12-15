package pl.su.su_backend.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String secretKey;

    @Getter
    @Value("${JWT_EXPIRATION}")
    private long jwtExpiration;

    @Getter
    @Value("${JWT_REFRESH_EXPIRATION}")
    private long refreshExpiration;

    @Value("${app.activation.expiration-ms:10800000}")
    private long activationExpiration;

    private SecretKey cachedSignInKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.cachedSignInKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String email, String fullName) {
        Map<String, Object> extraClaims = (fullName != null && !fullName.isEmpty())
                ? Map.of("name", fullName, "email", email)
                : Map.of("email", email);

        return generateToken(extraClaims, email);
    }

    public String generateToken(Map<String, Object> extraClaims, String email) {
        return buildToken(extraClaims, email, jwtExpiration);
    }

    public String generateRefreshToken(String email) {
        return buildToken(Map.of(), email, refreshExpiration);
    }

    public String generateActivationToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("typ", "activation")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + activationExpiration))
                .signWith(cachedSignInKey, Jwts.SIG.HS256)
                .compact();
    }

    private String buildToken(Map<String, Object> extraClaims, String email, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(cachedSignInKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isActivationToken(String token) {
        try {
            final String type = extractClaim(token, claims -> claims.get("typ", String.class));
            return "activation".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(cachedSignInKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}