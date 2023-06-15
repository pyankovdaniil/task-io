package microservices.authentication.validation;

import org.springframework.stereotype.Component;

@Component
public class EmailValidator {
    public boolean isValidEmail(String email) {
        return org.apache.commons.validator.routines.EmailValidator
                .getInstance().isValid(email);
    }
}
