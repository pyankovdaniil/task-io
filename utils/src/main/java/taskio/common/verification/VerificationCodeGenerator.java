package taskio.common.verification;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeGenerator {
    public String generateVerificationCode(int length) {
        return RandomStringUtils.random(length, true, true);
    }
}
