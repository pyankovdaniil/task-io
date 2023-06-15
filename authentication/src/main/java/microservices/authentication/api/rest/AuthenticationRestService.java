package microservices.authentication.api.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import microservices.authentication.dto.authenticate.AuthenticationRequest;
import microservices.authentication.dto.authenticate.AuthenticationTokensResponse;
import microservices.authentication.dto.refresh.RefreshTokensRequest;
import microservices.authentication.dto.refresh.RefreshTokensResponse;
import microservices.authentication.dto.register.RegistrationRequest;
import microservices.authentication.jwt.TokensGenerationResponse;
import microservices.authentication.jwt.TokensGenerator;
import microservices.authentication.mapping.ObjectMapperWrapper;
import microservices.authentication.user.User;
import microservices.authentication.user.UserRepository;
import microservices.authentication.user.UserRole;
import microservices.authentication.validation.EmailValidator;

@Service
@RequiredArgsConstructor
public class AuthenticationRestService {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationRestService.class);

    private final ObjectMapperWrapper objectMapper;
    private final UserRepository userRepository;
    private final EmailValidator emailValidator;
    private final TokensGenerator tokensGenerator;
    private final PasswordEncoder passwordEncoder;
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

    public Optional<AuthenticationTokensResponse> authenticate(AuthenticationRequest request) {
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
        logger.info("Successfully generated tokens for user:\n{}",
                objectMapper.toPrettyJson(userInDatabase.get()));

        return Optional.of(AuthenticationTokensResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .build());
    }

    public Optional<RefreshTokensResponse> refresh(RefreshTokensRequest request) {
        redisTemplate.opsForValue().set("harry", "potter");
        return Optional.empty();
    }
}
