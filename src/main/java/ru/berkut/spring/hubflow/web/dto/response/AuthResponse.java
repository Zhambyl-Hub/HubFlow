package ru.berkut.spring.hubflow.web.dto.response;


public record AuthResponse(
    String accessToken,
    String refreshToken
) {}
