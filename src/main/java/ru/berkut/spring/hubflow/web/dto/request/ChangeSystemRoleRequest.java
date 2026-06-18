package ru.berkut.spring.hubflow.web.dto.request;

import ru.berkut.spring.hubflow.enums.SystemRole;
import jakarta.validation.constraints.NotNull;

public record ChangeSystemRoleRequest(
    @NotNull SystemRole role
) {}
