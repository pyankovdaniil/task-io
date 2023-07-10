package taskio.microservices.authentication.api.rest;

import taskio.common.dto.authentication.authenticate.AuthenticationRequest;
import taskio.common.dto.authentication.authenticate.AuthenticationResponse;
import taskio.common.dto.authentication.refresh.RefreshResponse;
import taskio.common.dto.authentication.register.RegistrationRequest;
import taskio.common.dto.authentication.verify.EmailVerificationRequest;
import taskio.common.model.authentication.User;

public interface AuthenticationService {
    void register(RegistrationRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    RefreshResponse refresh(String bearerToken);
    void logout(String bearerToken);
    User getUserData(String bearerToken);
    void verify(EmailVerificationRequest request);
    User getUserDataFromEmail(String email);
}
