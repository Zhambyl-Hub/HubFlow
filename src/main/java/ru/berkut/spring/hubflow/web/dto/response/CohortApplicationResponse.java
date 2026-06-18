package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.CohortApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record CohortApplicationResponse(
    UUID                    id,
    UUID                    cohortId,
    String                  cohortTitle,
    UUID                    teamId,
    String                  teamName,
    CohortApplicationStatus status,
    Instant                 appliedAt,
    Instant                 reviewedAt
) {}
