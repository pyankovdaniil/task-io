package taskio.microservices.authentication.api.rest.handling;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import taskio.common.dto.errors.data.DataFieldError;
import taskio.common.dto.errors.data.InvalidDataResponse;
import taskio.common.dto.errors.logic.ErrorCode;
import taskio.common.dto.errors.logic.ErrorEntity;
import taskio.common.dto.errors.logic.ErrorResponse;
import taskio.common.exceptions.BaseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestControllerAdvice
public class AuthenticationRestControllerAdvice {
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoHandlerFound(NoHandlerFoundException exception) {
        return ErrorResponse.builder()
                .numberOfErrors(1)
                .errors(List.of(ErrorEntity.builder()
                        .errorCode(ErrorCode.INVALID_REQUEST_PATH)
                        .errorDate(new Date(System.currentTimeMillis()))
                        .errorMessage(exception.getMessage())
                        .dataCausedError(exception.getRequestURL())
                        .build()))
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public InvalidDataResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<DataFieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> fieldErrors.add(DataFieldError.builder()
                .fieldName(((FieldError) error).getField())
                .fieldError(error.getDefaultMessage())
                .build()));

        return InvalidDataResponse.builder()
                .fieldErrorsNumber(fieldErrors.size())
                .fieldErrors(fieldErrors)
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BaseException.class)
    public ErrorResponse handleUserAlreadyExistException(BaseException exception) {
        return ErrorResponse.builder()
                .numberOfErrors(1)
                .errors(List.of(ErrorEntity.builder()
                        .errorCode(exception.getErrorCode())
                        .errorDate(exception.getErrorDate())
                        .errorMessage(exception.getErrorMessage())
                        .dataCausedError(exception.getDataCausedError())
                        .build()))
                .build();
    }
}
