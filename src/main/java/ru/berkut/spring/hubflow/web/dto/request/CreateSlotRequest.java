package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record CreateSlotRequest(
    @NotNull Instant slotStart,
    @Min(15) @Max(120) int durationMinutes
) {}
