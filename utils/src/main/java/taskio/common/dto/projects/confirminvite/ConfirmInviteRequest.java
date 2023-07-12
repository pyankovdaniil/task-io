package taskio.common.dto.projects.confirminvite;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmInviteRequest {
    @Email(message = "Please, type the correct email")
    private String email;

    @Pattern(regexp = "[A-Z0-9]{8}", message = "Invite confirmation code should consist only of " +
            "capital letter and number. Length should be 8 characters")
    private String inviteConfirmationCode;

    @NotBlank(message = "Project identifier should not be blank")
    private String projectIdentifier;
}
