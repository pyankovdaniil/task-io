package taskio.microservices.authentication.jwt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokensGenerationResponse {
    private String accessToken;
    private String refreshToken;
}
