package microservices.authentication.dto.register;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String email;
    private String password;
    private String fullName;
}
