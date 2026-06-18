package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record CreateDemoDayRequest(
    @NotNull Instant eventDate,
    String description
) {}
