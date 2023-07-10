package taskio.microservices.authentication.api.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import taskio.common.dto.authentication.authenticate.AuthenticationRequest;
import taskio.common.dto.authentication.authenticate.AuthenticationResponse;
import taskio.common.dto.authentication.refresh.RefreshResponse;
import taskio.common.dto.authentication.register.RegistrationRequest;
import taskio.common.dto.authentication.verify.EmailVerificationRequest;
import taskio.common.dto.notification.NotificationRequest;
import taskio.common.exceptions.mail.InvalidEmailVerificationCodeException;
import taskio.common.exceptions.user.InvalidTokenException;
import taskio.common.exceptions.user.PasswordNotMatchException;
import taskio.common.exceptions.user.UserAlreadyExistException;
import taskio.common.exceptions.user.UserNotFoundException;
import taskio.common.model.authentication.User;
import taskio.common.model.authentication.UserNotVerified;
import taskio.common.uuid.CustomCompositeUuidGenerator;
import taskio.common.verification.EmailVerificationCodeGenerator;
import taskio.configs.amqp.RabbitMQMessageProducer;
import taskio.microservices.authentication.jwt.JwtService;
import taskio.microservices.authentication.jwt.TokensGenerationResponse;
import taskio.microservices.authentication.jwt.TokensGenerator;
import taskio.microservices.authentication.user.UserNotVerifiedRepository;
import taskio.microservices.authentication.user.UserRepository;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationRestService implements AuthenticationService {
    @Value("${request.auth-header-prefix}")
    private String authHeaderPrefix;

    @Value("${email.verification.verification-code-length}")
    private int emailVerificationCodeLength;

    @Value("${rabbitmq.exchanges.taskio-internal}")
    private String rabbitExchange;

    @Value("${rabbitmq.routing-keys.internal-notification}")
    private String rabbitRoutingKey;

    private final UserRepository userRepository;
    private final UserNotVerifiedRepository userNotVerifiedRepository;
    private final TokensGenerator tokensGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailVerificationCodeGenerator emailVerificationCodeGenerator;
    private final RabbitMQMessageProducer messageProducer;
    private final CustomCompositeUuidGenerator uuidGenerator;

    @Override
    public void register(RegistrationRequest request) {
        Optional<User> checkEmailUser = userRepository.findUserByEmail(request.getEmail());
        if (checkEmailUser.isPresent()) {
            throw new UserAlreadyExistException("User with this email is already registered");
        }

        Optional<UserNotVerified> userNotVerifiedCheck = userNotVerifiedRepository
                .findUserNotVerifiedByEmail(request.getEmail());
        if (userNotVerifiedCheck.isPresent()) {
            throw new UserAlreadyExistException("You have already send /register request, now you need" +
                    " to verify your email!");
        }

        String emailVerificationCode = emailVerificationCodeGenerator
                .generateVerificationCode(emailVerificationCodeLength).toUpperCase();

        log.info("Created email verification code: {}", emailVerificationCode);

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .toEmail(request.getEmail())
                .subject("Your task.io email verification code!")
                .text("Dear " + request.getFullName() + ",\nHere is your 6-digit email verification code: "
                        + emailVerificationCode)
                .build();

        messageProducer.publish(notificationRequest, rabbitExchange, rabbitRoutingKey);

        UserNotVerified userNotVerified = UserNotVerified.builder()
                .id(uuidGenerator.generateCustomUuid(UserNotVerified.class.getSimpleName()))
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .verificationCode(emailVerificationCode)
                .createdAt(new Date(System.currentTimeMillis()))
                .build();

        userNotVerifiedRepository.save(userNotVerified);
    }

    @Override
    public void verify(EmailVerificationRequest request) {
        Optional<UserNotVerified> userNotVerified = userNotVerifiedRepository
                .findUserNotVerifiedByEmail(request.getEmail());

        if (userNotVerified.isEmpty()) {
            throw new UserNotFoundException("Verification code on that email was not sent. Please, send" +
                    " /register request first!");
        }

        if (!userNotVerified.get().getVerificationCode().equals(request.getEmailVerificationCode())) {
            throw new InvalidEmailVerificationCodeException("This is incorrect email verification code." +
                    " Please, check you email again");
        }

        User user = User.builder()
                .id(uuidGenerator.generateCustomUuid(User.class.getSimpleName()))
                .email(request.getEmail())
                .password(userNotVerified.get().getPassword())
                .fullName(userNotVerified.get().getFullName())
                .build();

        userNotVerifiedRepository.delete(userNotVerified.get());
        userRepository.save(user);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Optional<User> userInDatabase = userRepository.findUserByEmail(request.getEmail());
        if (userInDatabase.isEmpty()) {
            throw new UserNotFoundException("User with this email is not in database");
        }

        if (!passwordEncoder.matches(request.getPassword(), userInDatabase.get().getPassword())) {
            throw new PasswordNotMatchException("Invalid password");
        }

        TokensGenerationResponse tokens = tokensGenerator.generateTokensForUser(userInDatabase.get());
        redisTemplate.opsForValue().set(request.getEmail(), tokens.getRefreshToken(),
                Duration.ofMillis(jwtService.getRefreshTokenExpireTimeMs()));

        return AuthenticationResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .build();
    }

    @Override
    public RefreshResponse refresh(String bearerToken) {
        String email = extractEmailFromToken(bearerToken);
        checkSavedRefreshToken(email);

        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User with this email is not in database");
        }

        return RefreshResponse.builder()
                .accessToken(jwtService.generateAccessToken(user.get())).build();
    }

    @Override
    public void logout(String bearerToken) {
        String email = extractEmailFromToken(bearerToken);
        Optional<String> savedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(email));
        if (savedRefreshToken.isEmpty()) {
            throw new InvalidTokenException("You are already logged out");
        }

        redisTemplate.opsForValue().getAndDelete(email);
    }

    @Override
    public User getUserData(String bearerToken) {
        String email = extractEmailFromToken(bearerToken);
        checkSavedRefreshToken(email);

        return getUserFromRepository(email);
    }

    @Override
    public User getUserDataFromEmail(String email) {
        return getUserFromRepository(email);
    }

    private String extractEmailFromToken(String bearerToken) {
        String refreshToken = bearerToken.substring(authHeaderPrefix.length() + 1);

        Optional<String> email = jwtService.extractEmailFromToken(refreshToken);

        if (email.isEmpty()) {
            throw new InvalidTokenException("Invalid or expired access token, can not extract user");
        }

        return email.get();
    }

    private void checkSavedRefreshToken(String email) {
        Optional<String> savedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(email));
        if (savedRefreshToken.isEmpty()) {
            throw new InvalidTokenException("Can not find your refresh token, please authenticate again");
        }
    }

    private User getUserFromRepository(String email) {
        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User with this email is not in database");
        }

        return user.get();
    }
}
