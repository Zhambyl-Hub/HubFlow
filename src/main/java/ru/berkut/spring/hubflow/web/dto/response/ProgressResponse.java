package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.ProgressStatus;
import java.time.Instant;
import java.util.UUID;

public record ProgressResponse(
    UUID           id,
    UUID           checkpointId,
    String         checkpointTitle,
    UUID           teamId,
    ProgressStatus status,
    String         proofUrl,
    String         comment,
    Instant        completedAt
) {}
