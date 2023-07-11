package taskio.microservices.projects.project;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import taskio.common.model.authentication.User;
import taskio.common.model.projects.ProjectMemberNotVerified;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberNotVerifiedRepository extends MongoRepository<ProjectMemberNotVerified, String> {
    List<ProjectMemberNotVerified> findAllByUser(User user);
}
