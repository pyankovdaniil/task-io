package taskio.microservices.authentication.api.rest.handling;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import taskio.common.dto.authentication.message.ResponseMessage;
import taskio.common.exceptions.mail.InvalidEmailVerificationCodeException;
import taskio.common.exceptions.user.InvalidTokenException;
import taskio.common.exceptions.user.PasswordNotMatchException;
import taskio.common.exceptions.user.UserAlreadyExistException;
import taskio.common.exceptions.user.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AuthenticationRestControllerAdvice {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseMessage noHandlerFound() {
        return ResponseMessage.withMessage("Sorry, this path is incorrect, please, check it again :(");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidEmailVerificationCodeException.class)
    public ResponseMessage handleInvalidEmailVerificationCodeException(InvalidEmailVerificationCodeException exception) {
        return ResponseMessage.withMessage(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseMessage handleUserAlreadyExist(UserAlreadyExistException exception) {
        return ResponseMessage.withMessage(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseMessage handlePasswordNotMatchException(PasswordNotMatchException exception) {
        return ResponseMessage.withMessage(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseMessage handleUserNotFound(UserNotFoundException exception) {
        return ResponseMessage.withMessage(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseMessage handleInvalidToken(InvalidTokenException exception) {
        return ResponseMessage.withMessage(exception.getMessage());
    }
}
