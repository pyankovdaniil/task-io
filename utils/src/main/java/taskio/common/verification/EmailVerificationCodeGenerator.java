package taskio.common.verification;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationCodeGenerator {
    public String generateHexVerificationCode(int length) {
        return RandomStringUtils.random(length, true, true);
    }
}
