package taskio.common.exceptions;

import org.springframework.validation.BindingResult;
import taskio.common.dto.errors.ErrorCode;

import java.util.Date;

public class BaseException extends RuntimeException {
    private ErrorCode errorCode;
    private Date errorDate;
    private String message;
    private Object dataCausedError;
    private BindingResult bindingResult;
}
