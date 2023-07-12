package taskio.common.model.projects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "project_data")
public class Project {
    @Id
    private String id;
    private String projectIdentifier;
    private String name;
    private String description;
    private String creatorEmail;
    private Date creationDate;

    @DBRef(lazy = true)
    private List<ProjectMember> members;
}
