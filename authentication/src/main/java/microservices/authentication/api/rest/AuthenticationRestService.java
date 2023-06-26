package microservices.authentication.api.rest;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import microservices.authentication.jwt.JwtService;
import microservices.authentication.jwt.TokensGenerationResponse;
import microservices.authentication.jwt.TokensGenerator;
import microservices.authentication.user.UserRepository;
import taskio.common.dto.authentication.authenticate.AuthenticationRequest;
import taskio.common.dto.authentication.authenticate.AuthenticationResponse;
import taskio.common.dto.authentication.refresh.RefreshResponse;
import taskio.common.dto.authentication.register.RegistrationRequest;
import taskio.common.exceptions.user.InvalidTokenException;
import taskio.common.exceptions.user.PasswordNotMatchException;
import taskio.common.exceptions.user.UserAlreadyExistException;
import taskio.common.exceptions.user.UserNotFoundException;
import taskio.common.model.authentication.User;

@Service
@RequiredArgsConstructor
public class AuthenticationRestService {
    @Value("${request.auth-header-prefix}")
    private String authHeaderPrefix;

    private final UserRepository userRepository;
    private final TokensGenerator tokensGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    public void register(RegistrationRequest request) {
        Optional<User> checkEmailUser = userRepository.findUserByEmail(request.getEmail());
        if (checkEmailUser.isPresent()) {
            throw new UserAlreadyExistException("User with this email is alreay registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        userRepository.save(user);
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
        Optional<String> email = jwtService.extractEmailFromToken(bearerToken
                .substring(authHeaderPrefix.length() + 1));

        if (email.isEmpty()) {
            throw new InvalidTokenException("Invalid or expired access token, can not extract user");
        }

        Optional<String> savedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(email.get()));
        if (savedRefreshToken.isEmpty()) {
            throw new InvalidTokenException("Can not find your refresh token, please authenticate again");
        }

        Optional<User> user = userRepository.findUserByEmail(email.get());
        if (user.isEmpty()) {
            throw new UserNotFoundException("User with this email is not in database");
        }

        return RefreshResponse.builder()
                .accessToken(jwtService.generateAccessToken(user.get())).build();

    }

    public void logout(String bearerToken) {
        Optional<String> email = jwtService.extractEmailFromToken(bearerToken
                .substring(authHeaderPrefix.length() + 1));

        if (email.isEmpty()) {
            throw new InvalidTokenException("Invalid or expired access token, can not extract user");
        }

        Optional<String> savedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(email.get()));
        if (savedRefreshToken.isEmpty()) {
            throw new InvalidTokenException("You are already logged out");
        }

        redisTemplate.opsForValue().getAndDelete(email.get());
    }

    public User getUserData(String bearerToken) {
        Optional<String> email = jwtService.extractEmailFromToken(bearerToken
                .substring(authHeaderPrefix.length() + 1));

        if (email.isEmpty()) {
            throw new InvalidTokenException("Invalid or expired access token, can not extract user");
        }

        Optional<String> savedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(email.get()));
        if (savedRefreshToken.isEmpty()) {
            throw new InvalidTokenException("Can not find your refresh token, please authenticate again");
        }

        Optional<User> user = userRepository.findUserByEmail(email.get());
        if (user.isEmpty()) {
            throw new UserNotFoundException("User with this email is not in database");
        }

        return user.get();
    }
}
