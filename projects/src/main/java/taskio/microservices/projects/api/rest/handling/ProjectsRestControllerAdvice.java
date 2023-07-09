package taskio.microservices.projects.api.rest.handling;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import taskio.common.dto.authentication.message.ResponseMessage;
import taskio.common.exceptions.user.UserAlreadyCreatedProjectException;
import taskio.common.exceptions.user.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ProjectsRestControllerAdvice {
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
    @ExceptionHandler(UserAlreadyCreatedProjectException.class)
    public ResponseMessage handleUserAlreadyCreatedProjectException(UserAlreadyCreatedProjectException exception) {
        return ResponseMessage.withMessage(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseMessage handleUserNotFound(UserNotFoundException exception) {
        return ResponseMessage.withMessage(exception.getMessage());
    }
}
