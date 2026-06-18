package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.TeamRole;
import java.util.UUID;

public record TeamMemberResponse(
    UUID     userId,
    String   firstName,
    String   lastName,
    String   email,
    TeamRole role
) {}
