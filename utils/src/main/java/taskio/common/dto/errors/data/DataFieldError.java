package taskio.common.dto.errors.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataFieldError {
    private String fieldName;
    private String fieldError;
}
