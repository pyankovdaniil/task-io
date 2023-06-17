package taskio.common.validation;


public class EmailValidator {
    public boolean isValidEmail(String email) {
        return org.apache.commons.validator.routines.EmailValidator
                .getInstance().isValid(email);
    }
}
