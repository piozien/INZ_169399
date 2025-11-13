package pl.su.su_backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
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
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration));
        
        if (fullName != null && !fullName.isEmpty()) {
            builder.claim("name", fullName);
        }
        builder.claim("email", email);
        
        return builder.signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateActivationToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("typ", "activation")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + activationExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
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
        String tokenEmail = extractEmail(token);
        return tokenEmail.equals(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
