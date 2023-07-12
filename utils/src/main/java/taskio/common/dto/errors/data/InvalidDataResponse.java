package taskio.common.dto.errors.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidDataResponse {
    private int fieldErrorsNumber;
    private List<DataFieldError> fieldErrors;
}
