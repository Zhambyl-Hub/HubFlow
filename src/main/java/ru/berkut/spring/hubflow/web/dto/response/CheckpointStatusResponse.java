package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.ProgressStatus;

import java.util.UUID;

public record CheckpointStatusResponse(
        UUID checkpointId,
        ProgressStatus status
) {
}
