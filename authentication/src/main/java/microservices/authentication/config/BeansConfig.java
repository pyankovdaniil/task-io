package microservices.authentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import taskio.common.mapping.ObjectMapperWrapper;
import taskio.common.validation.EmailValidator;

@Component
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
