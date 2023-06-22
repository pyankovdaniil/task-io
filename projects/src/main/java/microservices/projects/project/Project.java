package microservices.projects.project;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "projects_data")
public class Project {
    @Id
    private String id;
    private String name;
    private String creatorEmail;
    private Date creationDate;
}
