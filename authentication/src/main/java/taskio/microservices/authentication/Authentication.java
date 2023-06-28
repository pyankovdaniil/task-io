package taskio.microservices.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class }, scanBasePackages = {
    "taskio.microservices.authentication",
    "taskio.common",
    "taskio.configs"
})
public class Authentication {
    public static void main(String[] args) {
        SpringApplication.run(Authentication.class, args);
    }
}
