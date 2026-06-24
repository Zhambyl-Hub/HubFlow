package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddParticipantRequest(
    @NotNull UUID teamId,
    Integer presentationOrder
) {}
