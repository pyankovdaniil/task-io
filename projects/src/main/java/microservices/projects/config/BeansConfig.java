package microservices.projects.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import taskio.common.mapping.ObjectMapperWrapper;

@Configuration
public class BeansConfig {
    @Bean
    public ObjectMapperWrapper objectMapperWrapper() {
        return new ObjectMapperWrapper();
    }
}
