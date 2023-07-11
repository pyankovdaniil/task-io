package taskio.common.exceptions.user;

public class UserWasNotInvitedToProjectException extends RuntimeException {
    public UserWasNotInvitedToProjectException(String message) {
        super(message);
    }
}
