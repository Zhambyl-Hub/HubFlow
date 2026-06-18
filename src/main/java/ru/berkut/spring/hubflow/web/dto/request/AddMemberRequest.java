package ru.berkut.spring.hubflow.web.dto.request;

import ru.berkut.spring.hubflow.enums.MembershipRole;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record AddMemberRequest(
    @NotNull UUID userId,
    @NotNull MembershipRole role
) {}
