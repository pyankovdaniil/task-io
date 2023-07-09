package taskio.common.exceptions.mail;

public class InvalidEmailVerificationCodeException extends RuntimeException {
    public InvalidEmailVerificationCodeException(String message) {
        super(message);
    }
}
