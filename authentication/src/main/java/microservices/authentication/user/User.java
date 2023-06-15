package microservices.authentication.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "user_data")
@AllArgsConstructor
public class User {
    @Id
    private String id;
    private String email;
    private String password;
    private String fullName;
    private UserRole role;
}
