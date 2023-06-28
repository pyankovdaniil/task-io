package taskio.microservices.notification;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import taskio.configs.amqp.RabbitMQMessageProducer;
import taskio.microservices.notification.config.NotificationConfiguration;

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
