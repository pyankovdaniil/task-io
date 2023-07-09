package taskio.common.uuid;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomCompositeUuidGenerator {
    public String generateCustomUuid(String entitySimpleClassName) {
        String snakeCaseEntityClassName = entitySimpleClassName
                .replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
        return snakeCaseEntityClassName + ':' + UUID.randomUUID().toString();
    }
}
