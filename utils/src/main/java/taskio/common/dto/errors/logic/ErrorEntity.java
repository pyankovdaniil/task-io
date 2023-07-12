package taskio.common.dto.errors.logic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorEntity {
    private ErrorCode errorCode;
    private Date errorDate;
    private String errorMessage;
    private Object dataCausedError;
}
