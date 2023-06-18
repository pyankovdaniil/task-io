package microservices.projects.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import microservices.projects.clients.AuthenticationClient;
import taskio.common.dto.authentication.extractemail.ExtractEmailRequest;
import taskio.common.dto.projects.CreateRequest;

@Service
@RequiredArgsConstructor
public class ProjectsRestService {
    private final Logger logger = LoggerFactory.getLogger(ProjectsRestController.class);
    private final AuthenticationClient authenticationClient;

    @Value("${request.auth-header-prefix}")
    private String authHeaderPrefix;

    public boolean create(CreateRequest request, String bearerToken) {
        ExtractEmailRequest extractEmailRequest = ExtractEmailRequest.builder()
                .accessToken(bearerToken.substring(authHeaderPrefix.length() + 1)).build();

        try {
            ResponseEntity<?> emailResponse = authenticationClient.extractEmail(extractEmailRequest);
            if (emailResponse.getStatusCode().equals(HttpStatusCode.valueOf(HttpStatus.OK.value()))) {
                if (emailResponse.getBody() instanceof String email) {
                    logger.info("Successfully extracted email: {}", email);
                    return true;
                }
            }
            
            return false;
        } catch (Exception exception) {
            return false;
        }
    }
}
