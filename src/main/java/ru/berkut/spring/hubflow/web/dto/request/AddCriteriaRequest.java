package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.*;

public record AddCriteriaRequest(
    @NotBlank @Size(max = 255) String title,
    String description,
    @Min(1) @Max(100) int maxScore,
    @Min(0) int orderIndex
) {}
