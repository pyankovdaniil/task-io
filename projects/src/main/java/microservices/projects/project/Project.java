package microservices.projects.project;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    private String id;
    private String name;
    private String creatorId;
    private Date creationDate;
}
