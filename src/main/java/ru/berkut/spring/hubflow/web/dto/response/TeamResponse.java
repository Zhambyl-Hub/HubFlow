package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.*;
import java.time.Instant;
import java.util.UUID;

public record TeamResponse(
        UUID      id,
        String    name,
        String    ideaDescription,
        String    problem,
        String    targetSegment,
        String    solution,
        TeamStage stage,
        String    repoUrl,
        String    landingUrl,
        String    pitchUrl,
        Instant   createdAt
) {}
