package ru.berkut.spring.hubflow.web.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record WeekResponse(
    UUID             id,
    int              weekNumber,
    String           title,
    String           goal,
    LocalDate        startDate,
    LocalDate        endDate,
    List<CheckpointResponse> checkpoints
) {}
