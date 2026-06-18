package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.InviteStatus;
import java.time.Instant;
import java.util.UUID;

public record TeamInviteResponse(
    UUID         id,
    UUID         teamId,
    String       teamName,
    UUID         invitedUserId,
    String       invitedUserFirstName,
    String       invitedUserLastName,
    InviteStatus status,
    Instant      createdAt
) {}
