package taskio.common.dto.projects.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateRequest {
    @NotBlank(message = "Project name should not be blank")
    private String name;

    @NotBlank(message = "Project description should not be blank")
    private String description;
}
