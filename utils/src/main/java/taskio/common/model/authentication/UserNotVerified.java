package taskio.common.model.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_not_verified_data")
public class UserNotVerified {
    @Id
    private String id;
    private String email;
    private String password;
    private String fullName;
    private String verificationCode;

    @Indexed(name = "userNotVerifiedCreatedAtIndex", expireAfterSeconds = 60, unique = true)
    private Date createdAt;
}
