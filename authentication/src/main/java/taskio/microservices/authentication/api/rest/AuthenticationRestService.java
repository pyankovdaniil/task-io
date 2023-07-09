package taskio.microservices.authentication.api.rest;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import taskio.common.dto.authentication.authenticate.AuthenticationRequest;
import taskio.common.dto.authentication.authenticate.AuthenticationResponse;
import taskio.common.dto.authentication.refresh.RefreshResponse;
import taskio.common.dto.authentication.register.RegistrationRequest;
import taskio.common.dto.notification.NotificationRequest;
import taskio.common.exceptions.user.InvalidTokenException;
import taskio.common.exceptions.user.PasswordNotMatchException;
import taskio.common.exceptions.user.UserAlreadyExistException;
import taskio.common.exceptions.user.UserNotFoundException;
import taskio.common.mapping.ObjectMapperWrapper;
import taskio.common.model.authentication.User;
import taskio.common.model.authentication.UserNotVerified;
import taskio.common.verification.EmailVerificationCodeGenerator;
import taskio.configs.amqp.RabbitMQMessageProducer;
import taskio.microservices.authentication.jwt.JwtService;
import taskio.microservices.authentication.jwt.TokensGenerationResponse;
import taskio.microservices.authentication.jwt.TokensGenerator;
import taskio.microservices.authentication.user.UserNotVerifiedRepository;
import taskio.microservices.authentication.user.UserRepository;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationRestService {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationRestService.class);

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
    private final ObjectMapperWrapper objectMapper;
    private final RabbitMQMessageProducer messageProducer;

    public void register(RegistrationRequest request) {
        Optional<User> checkEmailUser = userRepository.findUserByEmail(request.getEmail());
        if (checkEmailUser.isPresent()) {
            throw new UserAlreadyExistException("User with this email is already registered");
        }

        String emailVerificationCode = emailVerificationCodeGenerator
                .generateVerificationCode(emailVerificationCodeLength).toUpperCase();

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .toEmail(request.getEmail())
                .subject("Your task.io email verification code!")
                .text("Dear, " + request.getFullName() + "!\nHere is your 6-digit verification code: "
                        + emailVerificationCode)
                .build();

        messageProducer.publish(notificationRequest, rabbitExchange, rabbitRoutingKey);

        UserNotVerified userNotVerified = UserNotVerified.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .verificationCode(emailVerificationCode)
                .build();

        logger.info("Send email verification code {} for saved not verified userNotVerified:\n{}",
                emailVerificationCode, objectMapper.toPrettyJson(userNotVerified));

        userNotVerifiedRepository.save(userNotVerified);

        logger.info("Successfully saved not verified user:\n{}", objectMapper.toPrettyJson(userNotVerified));
    }

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

    public void logout(String bearerToken) {
        String email = extractEmailFromToken(bearerToken);
        Optional<String> savedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(email));
        if (savedRefreshToken.isEmpty()) {
            throw new InvalidTokenException("You are already logged out");
        }

        redisTemplate.opsForValue().getAndDelete(email);
    }

    public User getUserData(String bearerToken) {
        String email = extractEmailFromToken(bearerToken);
        checkSavedRefreshToken(email);

        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User with this email is not in database");
        }

        return user.get();
    }

    private String extractEmailFromToken(String bearerToken) {
        Optional<String> email = jwtService.extractEmailFromToken(bearerToken
                .substring(authHeaderPrefix.length() + 1));

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
}
