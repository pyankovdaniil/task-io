package microservices.authentication.api.rest;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import microservices.authentication.jwt.JwtService;
import microservices.authentication.jwt.TokensGenerationResponse;
import microservices.authentication.jwt.TokensGenerator;
import microservices.authentication.user.User;
import microservices.authentication.user.UserRepository;
import microservices.authentication.user.UserRole;
import taskio.common.dto.authentication.authenticate.AuthenticationRequest;
import taskio.common.dto.authentication.authenticate.AuthenticationResponse;
import taskio.common.dto.authentication.extractemail.ExtractEmailRequest;
import taskio.common.dto.authentication.logout.LogoutRequest;
import taskio.common.dto.authentication.refresh.RefreshRequest;
import taskio.common.dto.authentication.refresh.RefreshResponse;
import taskio.common.dto.authentication.register.RegistrationRequest;
import taskio.common.dto.authentication.userdata.UserDataRequest;
import taskio.common.mapping.ObjectMapperWrapper;
import taskio.common.validation.EmailValidator;

@Service
@RequiredArgsConstructor
public class AuthenticationRestService {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationRestService.class);

    private final ObjectMapperWrapper objectMapper;
    private final UserRepository userRepository;
    private final EmailValidator emailValidator;
    private final TokensGenerator tokensGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    public boolean register(RegistrationRequest request) {
        if (!emailValidator.isValidEmail(request.getEmail())) {
            return false;
        }

        Optional<User> checkEmailUser = userRepository.findUserByEmail(request.getEmail());
        if (checkEmailUser.isPresent()) {
            return false;
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
        logger.info("Successfully saved user:\n{}",
                objectMapper.toPrettyJson(user));

        return true;
    }

    public Optional<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        if (!emailValidator.isValidEmail(request.getEmail())) {
            return Optional.empty();
        }

        Optional<User> userInDatabase = userRepository.findUserByEmail(request.getEmail());
        if (userInDatabase.isEmpty()) {
            logger.info("No such user with this email in database:\n{}",
                    request.getEmail());

            return Optional.empty();
        }

        if (!passwordEncoder.matches(request.getPassword(), userInDatabase.get().getPassword())) {
            logger.info("Incorrect password for user:\n{}", userInDatabase.get());
            return Optional.empty();
        }

        logger.info("Successfully found this user in database:\n{}",
                objectMapper.toPrettyJson(userInDatabase.get()));

        TokensGenerationResponse tokens = tokensGenerator.generateTokensForUser(userInDatabase.get());
        redisTemplate.opsForValue().set(request.getEmail(), tokens.getRefreshToken(),
                Duration.ofMillis(jwtService.getRefreshTokenExpireTimeMs()));
        logger.info("Successfully generated and saved to redis tokens for user:\n{}",
                objectMapper.toPrettyJson(userInDatabase.get()));

        return Optional.of(AuthenticationResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .build());
    }

    public Optional<RefreshResponse> refresh(RefreshRequest request) {
        Optional<String> email = jwtService.extractEmailFromToken(request.getRefreshToken());
        if (email.isPresent()) {
            Optional<String> savedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(email.get()));
            if (savedRefreshToken.isEmpty()) {
                return Optional.empty();
            }

            Optional<User> user = userRepository.findUserByEmail(email.get());
            if (user.isPresent()) {
                logger.info("Successfully found this user in database:\n{}",
                        objectMapper.toPrettyJson(user.get()));

                return Optional.of(RefreshResponse.builder()
                        .accessToken(jwtService.generateAccessToken(user.get())).build());
            }
        }

        return Optional.empty();
    }

    public boolean logout(LogoutRequest request) {
        Optional<String> email = jwtService.extractEmailFromToken(request.getRefreshToken());
        if (email.isPresent()) {
            Optional<String> savedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(email.get()));
            if (savedRefreshToken.isEmpty()) {
                return false;
            }

            redisTemplate.opsForValue().getAndDelete(email.get());
            logger.info("Successfully deleted resfresh token for user with email:\n{}",
                    email.get());
            
            return true;
        }

        return false;
    }

    public Optional<User> getUserData(UserDataRequest request) {
        Optional<String> email = jwtService.extractEmailFromToken(request.getRefreshToken());
        if (email.isPresent()) {
            Optional<String> savedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(email.get()));
            if (savedRefreshToken.isEmpty()) {
                return Optional.empty();
            }

            Optional<User> user = userRepository.findUserByEmail(email.get());
            if (user.isPresent()) {
                logger.info("Successfully found this user in database:\n{}",
                        objectMapper.toPrettyJson(user.get()));

                return user;
            }
        }

        return Optional.empty();
    }
}
