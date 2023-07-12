package taskio.common.dto.projects.leave;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveProjectRequest {
    @NotBlank(message = "Project identifier should not be blank")
    private String projectIdentifier;
}
