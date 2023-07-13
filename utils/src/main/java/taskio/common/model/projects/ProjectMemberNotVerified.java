package taskio.common.model.projects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import taskio.common.model.authentication.User;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_member_not_verified_data")
public class ProjectMemberNotVerified {
    @Id
    private String id;

    @DBRef(db = "users")
    private User user;
    private ProjectMemberRole role;
    private String verificationCode;

    @DBRef
    private Project project;
    private Date createdAt;
}
