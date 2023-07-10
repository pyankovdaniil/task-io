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
import taskio.common.dto.authentication.userdata.UserDataFromEmailRequest;
import taskio.common.dto.authentication.verify.EmailVerificationRequest;
import taskio.common.model.authentication.User;

@RestController
@RequestMapping("api/rest/v1/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {
    @Value("${request.auth-header-name}")
    private String authenticationHeaderName;
    private final AuthenticationRestService authenticationService;

    @PostMapping("/register")
    public ResponseMessage register(@Valid @RequestBody RegistrationRequest request) {
        authenticationService.register(request);
        return ResponseMessage.withMessage("Check your email for verification code! You have 60 seconds to" +
                " verify you email, after this time you should send /register request again!");
    }

    @PostMapping("/verify")
    public ResponseMessage verify(@Valid @RequestBody EmailVerificationRequest request) {
        authenticationService.verify(request);
        return ResponseMessage.withMessage("You have successfully verified your email. Now you can /authenticate" +
                " and get your access and refresh tokens!");
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
    public User getUserData(HttpServletRequest servletRequest) {
        String bearerToken = servletRequest.getHeader(authenticationHeaderName);
        return authenticationService.getUserData(bearerToken);
    }

    @PostMapping("/user-data-from-email")
    public User getUserData(@Valid @RequestBody UserDataFromEmailRequest request) {
        return authenticationService.getUserDataFromEmail(request.getEmail());
    }
}
