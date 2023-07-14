package taskio.common.dto.projects.confirmdelete;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmDeleteRequest {
    @Email(message = "Please, type the correct email")
    private String email;

    @Pattern(regexp = "[A-Z0-9]{12}", message = "Delete confirmation code should consist only of " +
            "capital letter and number. Length should be 12 characters")
    private String deleteConfirmationCode;

    @NotBlank(message = "Project identifier should not be blank")
    @Length(min = 6, message = "Project identifier length should me minimum 6 characters")
    private String projectIdentifier;
}
