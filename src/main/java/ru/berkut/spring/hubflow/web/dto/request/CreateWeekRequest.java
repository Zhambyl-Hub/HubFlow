package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateWeekRequest(
    @Min(1) int weekNumber,
    @NotBlank @Size(max = 255) String title,
    String goal,
    LocalDate startDate,
    LocalDate endDate
) {}
