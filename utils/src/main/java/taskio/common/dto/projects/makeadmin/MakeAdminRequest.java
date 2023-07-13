package taskio.common.dto.projects.makeadmin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeAdminRequest {
    @NotBlank(message = "Project identifier should not be blank")
    private String projectIdentifier;

    @Email(message = "Please, type the correct email")
    private String newAdminEmail;
}
