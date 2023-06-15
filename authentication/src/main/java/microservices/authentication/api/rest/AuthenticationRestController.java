package microservices.authentication.api.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import microservices.authentication.dto.authenticate.AuthenticationRequest;
import microservices.authentication.dto.authenticate.AuthenticationTokensResponse;
import microservices.authentication.dto.message.ResponseMessage;
import microservices.authentication.dto.refresh.RefreshTokensRequest;
import microservices.authentication.dto.refresh.RefreshTokensResponse;
import microservices.authentication.dto.register.RegistrationRequest;
import microservices.authentication.jwt.JwtService;
import microservices.authentication.mapping.ObjectMapperWrapper;

@RestController
@RequestMapping("rest/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationRestController.class);

    private final ObjectMapperWrapper objectMapper;
    private final AuthenticationRestService authenticationService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        logger.info("POST /register request received with data:\n{}",
                objectMapper.toPrettyJson(request));

        if (authenticationService.register(request)) {
            logger.info("User with email {} was successfully registrated",
                    request.getEmail());

            return ResponseEntity.ok(ResponseMessage
                    .withMessage("Registration was successful"));
        }

        return ResponseEntity.badRequest().body(ResponseMessage
                .withMessage("Invalid email or password"));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        logger.info("POST /authenticate request received with data:\n{}",
                objectMapper.toPrettyJson(request));

        Optional<AuthenticationTokensResponse> tokens = authenticationService.authenticate(request);
        if (tokens.isPresent()) {
            logger.info("User with email {} was successfully authenticated",
                    request.getEmail());

            return ResponseEntity.ok(tokens.get());
        }

        return ResponseEntity.badRequest().body(ResponseMessage
                .withMessage("Could not find user with these email and password"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokensRequest request) {
        logger.info("POST /refrest request received with data:\n{}",
                objectMapper.toPrettyJson(request));

        Optional<RefreshTokensResponse> tokens = authenticationService.refresh(request);
        if (tokens.isPresent()) {
            logger.info("Successfully refreshed token for user with email: {}",
                    jwtService.extractEmailFromToken(request.getRefreshToken()));

            return ResponseEntity.ok(tokens.get());
        }

        return ResponseEntity.badRequest().body(ResponseMessage
                .withMessage("Invalid or expired refresh token, can not refresh"));
    }
}
