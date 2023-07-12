package taskio.common.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import taskio.common.dto.errors.logic.ErrorCode;

import java.util.Date;

@AllArgsConstructor
@Getter
public abstract class BaseException extends RuntimeException {
    protected Date errorDate;
    protected String errorMessage;
    protected ErrorCode errorCode;
    protected Object dataCausedError;
    protected BindingResult bindingResult;
}
