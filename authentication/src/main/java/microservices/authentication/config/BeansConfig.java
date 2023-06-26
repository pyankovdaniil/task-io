package microservices.authentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import taskio.common.mapping.ObjectMapperWrapper;

@Component
public class BeansConfig {
    @Bean
    public ObjectMapperWrapper objectMapperWrapper() {
        return new ObjectMapperWrapper();
    }
}
