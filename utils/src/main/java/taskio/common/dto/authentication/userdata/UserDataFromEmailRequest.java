package taskio.common.dto.authentication.userdata;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataFromEmailRequest {
    @Email(message = "Please, type the correct email")
    private String email;
}
