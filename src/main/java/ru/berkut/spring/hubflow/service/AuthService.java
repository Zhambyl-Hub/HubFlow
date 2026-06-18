package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.exception.BadRequestException;
import ru.berkut.spring.hubflow.exception.ConflictException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.UserRepository;
import ru.berkut.spring.hubflow.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse buildTokensPublic(User user) {
        String access  = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refresh = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        return new AuthResponse(access, refresh, user.getId(), user.getEmail());
    }

    public record RegisterRequest(String email, String password,
                                  String firstName, String lastName, String phone) {}

    public record AuthResponse(String accessToken, String refreshToken, UUID userId, String email) {}

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered: " + req.email());
        }
        User user = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .firstName(req.firstName())
                .lastName(req.lastName())
                .phone(req.phone())
                .build();
        userRepository.save(user);
        return buildTokens(user);
    }

    public AuthResponse login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        return buildTokens(user);
    }

    public AuthResponse refreshTokens(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user   = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return buildTokens(user);
    }

    private AuthResponse buildTokens(User user) {
        String access  = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refresh = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        return new AuthResponse(access, refresh, user.getId(), user.getEmail());
    }
}
