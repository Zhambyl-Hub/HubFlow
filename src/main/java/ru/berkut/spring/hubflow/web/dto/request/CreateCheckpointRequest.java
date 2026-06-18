package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.*;

public record CreateCheckpointRequest(
    @NotBlank @Size(max = 255) String title,
    String description,
    boolean required,
    @Min(0) int orderIndex
) {}
