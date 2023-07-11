package taskio.common.dto.projects.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleProjectMembership {
    private String projectName;
    private String roleInProject;
}
