package taskio.common.dto.projects.leave;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveProjectRequest {
    @NotBlank(message = "Project identifier should not be blank")
    @Length(min = 6, message = "Project identifier length should me minimum 6 characters")
    private String projectIdentifier;
}
