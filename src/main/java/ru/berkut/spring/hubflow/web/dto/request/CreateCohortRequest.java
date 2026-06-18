package ru.berkut.spring.hubflow.web.dto.request;

import ru.berkut.spring.hubflow.enums.CohortFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateCohortRequest(
    @NotBlank @Size(max = 255) String title,
    String description,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @Min(1) @Max(52) int totalWeeks,
    @NotNull CohortFormat format
) {}
