package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.service.AuthService;
import ru.berkut.spring.hubflow.web.dto.request.LoginRequest;
import ru.berkut.spring.hubflow.web.dto.request.RegisterRequest;
import ru.berkut.spring.hubflow.web.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        var result = authService.register(new AuthService.RegisterRequest(
            req.email(), req.password(), req.firstName(), req.lastName(), req.phone()));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AuthResponse(result.accessToken(), result.refreshToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        var result = authService.login(req.email(), req.password());
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.refreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("X-Refresh-Token") String token) {
        var result = authService.refreshTokens(token);
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.refreshToken()));
    }
}
