package taskio.microservices.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "taskio.microservices.notification",
        "taskio.configs",
        "taskio.common"
})
public class Notification {
    public static void main(String[] args) {
        SpringApplication.run(Notification.class, args);
    }
}
