package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record VoteRequest(
    @NotNull UUID teamId,
    @NotNull UUID criterionId,
    @Min(1) int score
) {}
