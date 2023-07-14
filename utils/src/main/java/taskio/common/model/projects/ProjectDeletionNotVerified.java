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
@Document(collection = "project_deletions_not_verified_data")
public class ProjectDeletionNotVerified {
    @Id
    private String id;
    private String verificationCode;

    @DBRef(db = "users")
    private User user;

    @DBRef
    private Project project;
    private Date createdAt;
}
