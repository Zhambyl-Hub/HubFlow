package ru.berkut.spring.hubflow.web.dto.request;

import ru.berkut.spring.hubflow.enums.ProgressStatus;
import jakarta.validation.constraints.*;

public record MarkProgressRequest(
    @NotNull ProgressStatus status,
    @Size(max = 500) String proofUrl,
    String comment
) {}
