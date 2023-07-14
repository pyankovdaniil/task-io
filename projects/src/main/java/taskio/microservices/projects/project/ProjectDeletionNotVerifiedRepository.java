package taskio.microservices.projects.project;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import taskio.common.model.authentication.User;
import taskio.common.model.projects.ProjectDeletionNotVerified;

import java.util.List;

@Repository
public interface ProjectDeletionNotVerifiedRepository extends MongoRepository<ProjectDeletionNotVerified, String> {
    List<ProjectDeletionNotVerified> findAllByUser(User user);
}
