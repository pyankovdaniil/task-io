package taskio.common.dto.authentication.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistrationRequest {
    @Email(message = "Please, type the correct email")
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password should have minimum eight characters, at least one uppercase letter, " +
                    "one lowercase letter, one number and one special character (@$!%*?&)")
    private String password;

    @Pattern(regexp = "(?i)^[a-z ,.'-]+$", message = "Please, type the correct full name split with spaces")
    private String fullName;
}
