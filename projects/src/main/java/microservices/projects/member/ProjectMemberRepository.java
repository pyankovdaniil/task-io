package microservices.projects.member;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import taskio.common.model.projects.ProjectMember;

@Repository
public interface ProjectMemberRepository extends MongoRepository<ProjectMember, String> {
}
