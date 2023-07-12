package taskio.microservices.projects.project;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import taskio.common.model.projects.Project;

import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findProjectByProjectIdentifier(String projectIdentifier);
}
