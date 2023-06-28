package taskio.common.model.authentication;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
