package microservices.authentication.dto.message;

import lombok.Data;

@Data
public class ResponseMessage {
    private String message;

    private ResponseMessage(String message) {
        this.message = message;
    }

    public static ResponseMessage withMessage(String message) {
        return new ResponseMessage(message);
    }
}
