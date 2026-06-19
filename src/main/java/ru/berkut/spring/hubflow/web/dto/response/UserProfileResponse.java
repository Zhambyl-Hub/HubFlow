package ru.berkut.spring.hubflow.web.dto.response;
import ru.berkut.spring.hubflow.enums.SystemRole;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID       id,
        String     email,
        String     firstName,
        String     lastName,
        String     phone,
        String     avatarUrl,
        SystemRole systemRole,
        Instant    createdAt
) {}
