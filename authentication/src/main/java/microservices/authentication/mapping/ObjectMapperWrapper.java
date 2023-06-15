package microservices.authentication.mapping;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ObjectMapperWrapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toPrettyJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
