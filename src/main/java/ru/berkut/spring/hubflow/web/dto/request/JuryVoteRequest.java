package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JuryVoteRequest(
        @NotNull UUID teamId,
        @NotNull UUID criterionId,
        @Min(1)  int  score
) {}