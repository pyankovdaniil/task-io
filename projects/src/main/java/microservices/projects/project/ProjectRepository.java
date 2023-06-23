package microservices.projects.project;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import taskio.common.model.projects.Project;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
}
