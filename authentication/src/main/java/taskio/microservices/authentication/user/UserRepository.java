package taskio.microservices.authentication.user;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import taskio.common.model.authentication.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> { 
    Optional<User> findUserByEmail(String email);
}