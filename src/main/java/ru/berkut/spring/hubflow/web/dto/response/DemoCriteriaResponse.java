package ru.berkut.spring.hubflow.web.dto.response;

import java.util.UUID;

public record DemoCriteriaResponse(
    UUID   id,
    String title,
    String description,
    int    maxScore,
    int    orderIndex
) {}
