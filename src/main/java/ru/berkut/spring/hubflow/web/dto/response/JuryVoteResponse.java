package ru.berkut.spring.hubflow.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record JuryVoteResponse(
        UUID    id,
        UUID    teamId,
        String  teamName,
        UUID    criterionId,
        String  criterionTitle,
        int     score,
        Instant votedAt
) {}