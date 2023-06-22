package microservices.projects.member;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberRepository extends MongoRepository<ProjectMember, String> {
}
