package taskio.microservices.authentication.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokensGenerationResponse {
    private String accessToken;
    private String refreshToken;
}
