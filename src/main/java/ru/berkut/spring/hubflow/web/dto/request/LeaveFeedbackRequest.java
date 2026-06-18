package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.*;

public record LeaveFeedbackRequest(
    @NotBlank String content,
    @Min(1) @Max(5) int readinessScore
) {}
