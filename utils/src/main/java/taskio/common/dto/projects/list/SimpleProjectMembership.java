package taskio.common.dto.projects.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import taskio.common.model.projects.ProjectMemberRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleProjectMembership {
    private String projectIdentifier;
    private String projectName;
    private ProjectMemberRole roleInProject;
}
