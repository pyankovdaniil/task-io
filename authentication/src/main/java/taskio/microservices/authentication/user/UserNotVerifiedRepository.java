package taskio.microservices.authentication.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import taskio.common.model.authentication.UserNotVerified;

public interface UserNotVerifiedRepository extends MongoRepository<UserNotVerified, String> {
}
