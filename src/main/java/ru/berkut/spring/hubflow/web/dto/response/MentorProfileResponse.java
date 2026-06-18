package ru.berkut.spring.hubflow.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MentorProfileResponse(
    UUID    id,
    UUID    userId,
    String  firstName,
    String  lastName,
    String  email,
    String  bio,
    String  expertise,
    String  linkedinUrl,
    String  avatarUrl,
    Boolean isVisible,
    Instant updatedAt
) {}
