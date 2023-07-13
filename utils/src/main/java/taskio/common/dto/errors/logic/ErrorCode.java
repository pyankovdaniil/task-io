package taskio.common.dto.errors.logic;

public enum ErrorCode {
    USER_ALREADY_EXIST_IN_DATABASE,
    USER_NOT_FOUND_BY_EMAIL_IN_DATABASE,
    INVALID_TOKEN,
    PASSWORD_NOT_MATCH,
    USER_ALREADY_CREATED_THIS_PROJECT,
    USER_ALREADY_IS_IN_PROJECT,
    INVALID_EMAIL_VERIFICATION_CODE,
    CAN_NOT_SEND_EMAIL,
    INVITER_IS_NOT_MEMBER,
    USER_ALREADY_IN_PROJECT,
    USER_WAS_NOT_INVITED_TO_PROJECT,
    INVALID_REQUEST_PATH,
    USER_IS_NOT_IN_PROJECT,
    USER_IS_NOT_CREATOR,
    PROJECT_IDENTIFIER_IS_TAKEN,
    USER_IS_NOT_ALLOWED_TO_SET_ADMIN,
    INVALID_INVITE_CONFIRMATION_CODE,
    USER_IS_ALREADY_ADMIN,
    USER_CAN_NOT_LEAVE,
}
