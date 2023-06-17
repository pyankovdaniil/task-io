package microservices.projects.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import taskio.common.mapping.ObjectMapperWrapper;
import taskio.common.validation.EmailValidator;

@Configuration
public class BeansConfig {
    @Bean
    public ObjectMapperWrapper objectMapperWrapper() {
        return new ObjectMapperWrapper();
    }

    @Bean
    public EmailValidator emailValidator() {
        return new EmailValidator();
    }
}
