package microservices.authentication.jwt;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import taskio.common.model.authentication.User;

@Component
@RequiredArgsConstructor
public class TokensGenerator {
    private final JwtService jwtService;

    public TokensGenerationResponse generateTokensForUser(User user) {
        return TokensGenerationResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }
}
