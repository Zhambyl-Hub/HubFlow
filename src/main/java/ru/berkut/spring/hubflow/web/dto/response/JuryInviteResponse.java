package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.JuryInviteStatus;

import java.time.Instant;
import java.util.UUID;

public record JuryInviteResponse(
        UUID             id,
        UUID             demoDayId,
        String           name,
        String           email,
        String           inviteToken,
        String           inviteUrl,       // готовая ссылка для отправки
        JuryInviteStatus status,
        UUID             juryUserId,
        Instant          expiresAt,
        Instant          activatedAt,
        Instant          createdAt
) {}