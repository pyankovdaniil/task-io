package taskio.common.exceptions.mail;

public class CanNotSendEmailException extends RuntimeException {
    public CanNotSendEmailException(String message) {
        super(message);
    }
}
