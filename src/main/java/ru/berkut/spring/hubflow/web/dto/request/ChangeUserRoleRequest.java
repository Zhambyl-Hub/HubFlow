package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.NotNull;
import ru.berkut.spring.hubflow.enums.MembershipRole;

public record ChangeUserRoleRequest(
        @NotNull MembershipRole role
) {}