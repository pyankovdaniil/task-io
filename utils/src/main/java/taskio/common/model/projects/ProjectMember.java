package taskio.common.model.projects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import taskio.common.model.authentication.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_members_data")
public class ProjectMember {
    @Id
    private String id;

    @DBRef(db = "users")
    private User user;
    private String role;

    @DBRef
    private Project project;
}
