package taskio.microservices.projects.project;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import taskio.common.model.authentication.User;
import taskio.common.model.projects.ProjectMember;

@Repository
public interface ProjectMemberRepository extends MongoRepository<ProjectMember, String> {
    List<ProjectMember> findAllByUser(User user);
}
