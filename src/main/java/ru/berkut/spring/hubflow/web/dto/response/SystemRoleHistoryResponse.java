package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.SystemRole;

import java.time.Instant;
import java.util.UUID;

public record SystemRoleHistoryResponse(
        UUID id,
        UUID       userId,
        String     userName,
        SystemRole oldRole,
        SystemRole newRole,
        String     changedBy,
        Instant changedAt
) {}