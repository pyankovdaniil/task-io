package taskio.microservices.authentication.api.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taskio.common.dto.authentication.authenticate.AuthenticationRequest;
import taskio.common.dto.authentication.authenticate.AuthenticationResponse;
import taskio.common.dto.authentication.message.ResponseMessage;
import taskio.common.dto.authentication.refresh.RefreshResponse;
import taskio.common.dto.authentication.register.RegistrationRequest;
import taskio.common.model.authentication.User;

@RestController
@RequestMapping("rest/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {
    @Value("${request.auth-header-name}")
    private String authenticationHeaderName;
    private final AuthenticationRestService authenticationService;

    @PostMapping("/register")
    public ResponseMessage register(@Valid @RequestBody RegistrationRequest request) {
        authenticationService.register(request);
        return ResponseMessage.withMessage("Check your email for verification code!");
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(request);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(HttpServletRequest servletRequest) {
        String bearerToken = servletRequest.getHeader(authenticationHeaderName);
        return authenticationService.refresh(bearerToken);
    }

    @PostMapping("/logout")
    public ResponseMessage logout(HttpServletRequest servletRequest) {
        String bearerToken = servletRequest.getHeader(authenticationHeaderName);
        authenticationService.logout(bearerToken);
        return ResponseMessage.withMessage("Logout was successful");
    }

    @PostMapping("/user-data")
    public User userData(HttpServletRequest servletRequest) {
        String bearerToken = servletRequest.getHeader(authenticationHeaderName);
        return authenticationService.getUserData(bearerToken);
    }
}
