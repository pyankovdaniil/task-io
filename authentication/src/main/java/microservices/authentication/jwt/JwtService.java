package microservices.authentication.jwt;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import microservices.authentication.user.User;

@Component
public class JwtService {
    private final int JWT_EXPIRE_TIME_MS = 30 * 60 * 1000;
    private final int REFRESH_TOKEN_EXPIRE_TIME_MS = 2 * 24 * 60 * 60 * 1000;

    @Value("${jwt.secret-key}")
    private String secretKey;

    public Optional<String> extractEmailFromToken(String token) {
        try {
            return Optional.of(extractClaim(token, Claims::getSubject));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private <T> T extractClaim(String jwt, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(jwt));
    }

    private Claims extractAllClaims(String jwt) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(Map.of())
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRE_TIME_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(Map.of())
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
