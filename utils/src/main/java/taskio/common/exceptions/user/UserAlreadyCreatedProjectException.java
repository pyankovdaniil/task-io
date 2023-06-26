package taskio.common.exceptions.user;

public class UserAlreadyCreatedProjectException extends RuntimeException {
    public UserAlreadyCreatedProjectException(String message) {
        super(message);
    }
}
