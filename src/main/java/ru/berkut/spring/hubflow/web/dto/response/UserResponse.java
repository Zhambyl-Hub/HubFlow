package ru.berkut.spring.hubflow.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID    id,
    String  email,
    String  firstName,
    String  lastName,
    String  phone,
    String  avatarUrl,
    Boolean isActive,
    Instant createdAt
) {}
