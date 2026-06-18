package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.MembershipRole;
import java.time.Instant;
import java.util.UUID;

public record CohortMemberResponse(
    UUID           userId,
    String         firstName,
    String         lastName,
    String         email,
    MembershipRole role,
    Instant        joinedAt
) {}
