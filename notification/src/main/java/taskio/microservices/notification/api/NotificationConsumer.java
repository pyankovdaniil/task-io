package taskio.microservices.notification.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import taskio.common.dto.notification.NotificationRequest;
import taskio.common.mapping.ObjectMapperWrapper;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    private final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    private final ObjectMapperWrapper objectMapper;

    @RabbitListener(queues = "${spring.rabbitmq.queue.taskio-notification}")
    public void consume(NotificationRequest notificationRequest) {
        logger.info("Got notification request:\n{}",
                objectMapper.toPrettyJson(notificationRequest));
    }
}
