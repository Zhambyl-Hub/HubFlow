package ru.berkut.spring.hubflow.web.dto.response;

import java.util.UUID;

public record CheckpointResponse(
    UUID    id,
    String  title,
    String  description,
    boolean isRequired,
    int     orderIndex
) {}
