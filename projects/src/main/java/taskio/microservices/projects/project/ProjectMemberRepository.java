package taskio.microservices.projects.project;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import taskio.common.model.authentication.User;
import taskio.common.model.projects.ProjectMember;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends MongoRepository<ProjectMember, String> {
    List<ProjectMember> findAllByUser(User user);
}
