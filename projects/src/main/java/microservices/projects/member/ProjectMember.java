package microservices.projects.member;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_members_data")
public class ProjectMember {
    @Id
    private String id;
    private String email;
    private String fullName;
    private String role;
}
