package taskio.common.dto.errors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorEntity {
    private ErrorCode errorCode;
    private Date errorDate;
    private String message;
    private Object dataCausedError;
    private List<FieldError> fieldErrors;
}
