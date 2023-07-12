package taskio.common.dto.projects.id;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeProjectIdentifierRequest {
    @NotBlank(message = "Project identifier should not be blank")
    private String projectIdentifier;

    @NotBlank(message = "New project identifier invite id should not be blank")
    private String newProjectIdentifier;
}
