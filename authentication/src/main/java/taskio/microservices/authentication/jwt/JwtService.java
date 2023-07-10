package taskio.microservices.authentication.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import taskio.common.model.authentication.User;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
@Slf4j
public class JwtService {
    @Value("${jwt.access-token-expire-time-ms}")
    @Getter
    private Integer accessTokenExpireTimeMs;

    @Value("${jwt.refresh-token-expire-time-ms}")
    @Getter
    private Integer refreshTokenExpireTimeMs;

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
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpireTimeMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(Map.of())
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpireTimeMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
