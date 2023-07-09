package taskio.common.dto.authentication.verify;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {
    @Pattern(regexp = "[A-Z0-9]{6}", message = "Email verification code should consist only of " +
            "capital letter and number. Length should be 6 characters")
    private String emailVerificationCode;

    @Email(message = "Please, type the correct email")
    private String email;
}
