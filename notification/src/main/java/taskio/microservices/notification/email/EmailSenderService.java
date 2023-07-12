package taskio.microservices.notification.email;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import taskio.common.dto.errors.logic.ErrorCode;
import taskio.common.exceptions.mail.CanNotSendEmailException;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class EmailSenderService {
    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            mimeMessage.setFrom(new InternetAddress(from, "task.io"));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(body);
            mailSender.send(mimeMessage);
        } catch (Exception exception) {
            throw CanNotSendEmailException.builder()
                    .errorDate(new Date(System.currentTimeMillis()))
                    .errorMessage(exception.getMessage())
                    .errorCode(ErrorCode.USER_ALREADY_CREATED_PROJECT)
                    .dataCausedError(exception)
                    .build();
        }
    }
}
