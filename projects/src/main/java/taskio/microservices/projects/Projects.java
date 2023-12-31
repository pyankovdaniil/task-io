package taskio.microservices.projects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}, scanBasePackages = {
        "taskio.microservices.projects",
        "taskio.common",
        "taskio.configs"
})
@EnableFeignClients(basePackages = {"taskio.microservices.projects.clients"})
@PropertySources({
        @PropertySource("classpath:clients-kube.properties")
})
public class Projects {
    public static void main(String[] args) {
        SpringApplication.run(Projects.class, args);
    }
}
