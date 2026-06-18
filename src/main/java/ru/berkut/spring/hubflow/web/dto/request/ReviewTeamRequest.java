package ru.berkut.spring.hubflow.web.dto.request;

import ru.berkut.spring.hubflow.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ReviewTeamRequest(
    @NotNull ApplicationStatus status
) {}
