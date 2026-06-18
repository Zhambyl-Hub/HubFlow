package ru.berkut.spring.hubflow.web.dto.response;

import java.util.UUID;

public record CheckpointSummaryResponse(
        UUID id,
        String title,
        Integer weekNumber
) {
}