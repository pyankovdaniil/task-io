package taskio.microservices.authentication.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import taskio.common.model.authentication.User;
import taskio.common.model.authentication.UserNotVerified;

import java.util.Optional;

public interface UserNotVerifiedRepository extends MongoRepository<UserNotVerified, String> {
    Optional<UserNotVerified> findUserNotVerifiedByEmail(String email);
}
