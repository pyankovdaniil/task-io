package taskio.common.exceptions.projects;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import taskio.common.dto.errors.logic.ErrorCode;
import taskio.common.exceptions.BaseException;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Getter
public class UserCanNotLeaveException extends BaseException {
    @Builder
    public UserCanNotLeaveException(Date errorDate, String errorMessage, ErrorCode errorCode, Object dataCausedError, BindingResult bindingResult) {
        super(errorDate, errorMessage, errorCode, dataCausedError, bindingResult);
    }
}
