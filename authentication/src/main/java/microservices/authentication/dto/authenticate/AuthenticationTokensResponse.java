package microservices.authentication.dto.authenticate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationTokensResponse {
    private String accessToken;
    private String refreshToken;
}
