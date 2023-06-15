package microservices.authentication.dto.refresh;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokensResponse {
    private String accessToken;
}
