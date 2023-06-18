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
import microservices.authentication.jwt.JwtService;
import microservices.authentication.user.User;
import taskio.common.dto.authentication.authenticate.AuthenticationRequest;
import taskio.common.dto.authentication.authenticate.AuthenticationResponse;
import taskio.common.dto.authentication.extractemail.ExtractEmailRequest;
import taskio.common.dto.authentication.logout.LogoutRequest;
import taskio.common.dto.authentication.message.ResponseMessage;
import taskio.common.dto.authentication.refresh.RefreshRequest;
import taskio.common.dto.authentication.refresh.RefreshResponse;
import taskio.common.dto.authentication.register.RegistrationRequest;
import taskio.common.dto.authentication.userdata.UserDataRequest;
import taskio.common.mapping.ObjectMapperWrapper;

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

        Optional<AuthenticationResponse> tokens = authenticationService.authenticate(request);
        if (tokens.isPresent()) {
            logger.info("User with email {} was successfully authenticated",
                    request.getEmail());

            return ResponseEntity.ok(tokens.get());
        }

        return ResponseEntity.badRequest().body(ResponseMessage
                .withMessage("Could not find user with these email and password"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        logger.info("POST /refrest request received with data:\n{}",
                objectMapper.toPrettyJson(request));

        Optional<RefreshResponse> tokens = authenticationService.refresh(request);
        if (tokens.isPresent()) {
            logger.info("Successfully refreshed token for user with email: {}",
                    jwtService.extractEmailFromToken(request.getRefreshToken()));

            return ResponseEntity.ok(tokens.get());
        }

        return ResponseEntity.badRequest().body(ResponseMessage
                .withMessage("Invalid or expired refresh token, can not refresh"));
    }

    @PostMapping("/extract-email")
    public ResponseEntity<?> extractEmail(@RequestBody ExtractEmailRequest request) {
        logger.info("POST /extract-email request received with data:\n{}",
                objectMapper.toPrettyJson(request));

        Optional<String> email = jwtService.extractEmailFromToken(request.getAccessToken());
        if (email.isPresent()) {
            logger.info("Successfully extracted email for user with email:\n{}",
                    email.get());
            
            return ResponseEntity.ok(email.get());
        }

        return ResponseEntity.badRequest().body(ResponseMessage
        .withMessage("Can not extract email. Invalid or expired access token"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        logger.info("POST /logout request received with data:\n{}",
                objectMapper.toPrettyJson(request));

        if (authenticationService.logout(request)) {
            logger.info("User with email {} was successfully logged out",
                    jwtService.extractEmailFromToken(request.getRefreshToken()));

            return ResponseEntity.ok(ResponseMessage
                    .withMessage("Logout was successful"));
        }

        return ResponseEntity.badRequest().body(ResponseMessage
                .withMessage("Could not logout, expired or invalid refresh token"));
    }

    @PostMapping("/user-data")
    public ResponseEntity<?> userData(@RequestBody UserDataRequest request) {
        logger.info("POST /user-data request received with data:\n{}",
                objectMapper.toPrettyJson(request));

        Optional<User> user = authenticationService.getUserData(request);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }

        return ResponseEntity.badRequest().body(ResponseMessage
                .withMessage("Could not find user, expired or invalid refresh token"));
    }
}
