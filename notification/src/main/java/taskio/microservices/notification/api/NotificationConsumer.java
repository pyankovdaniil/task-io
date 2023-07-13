package taskio.microservices.notification.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import taskio.common.dto.notification.NotificationRequest;
import taskio.common.mapping.ObjectMapperWrapper;
import taskio.microservices.notification.email.EmailSenderService;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final ObjectMapperWrapper objectMapper;
    private final EmailSenderService emailSenderService;

    @RabbitListener(queues = "${spring.rabbitmq.queue.notification}")
    public void consume(NotificationRequest notificationRequest) {
        log.info("Got notification request:\n{}",
                objectMapper.toPrettyJson(notificationRequest));

        emailSenderService.sendEmail(notificationRequest.getToEmail(), notificationRequest.getSubject(),
                 notificationRequest.getText());

        log.info("Successfully send email from request:\n{}",
                objectMapper.toPrettyJson(notificationRequest));
    }
}
