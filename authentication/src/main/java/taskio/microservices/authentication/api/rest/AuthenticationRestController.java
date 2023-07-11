package taskio.microservices.authentication.api.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
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
    private final AuthenticationRestService authenticationService;

    @PostMapping("/register")
    public ResponseMessage register(@Valid @RequestBody RegistrationRequest request) {
        authenticationService.register(request);
        return ResponseMessage.withMessage("Check your email for verification code! You have 60 seconds to" +
                " verify you email, after this time you should send /register request again!");
    }

    @PostMapping("/verify-email")
    public ResponseMessage verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        authenticationService.verifyEmail(request);
        return ResponseMessage.withMessage("You have successfully verified your email. Now you can /authenticate" +
                " and get your access and refresh tokens!");
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(request);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        return authenticationService.refresh(bearerToken);
    }

    @PostMapping("/logout")
    public ResponseMessage logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        authenticationService.logout(bearerToken);
        return ResponseMessage.withMessage("Logout was successful");
    }

    @PostMapping("/user-data")
    public User getUserData(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        return authenticationService.getUserData(bearerToken);
    }

    @PostMapping("/user-data-from-email")
    public User getUserData(@Valid @RequestBody UserDataFromEmailRequest request) {
        return authenticationService.getUserDataFromEmail(request.getEmail());
    }
}
