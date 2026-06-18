package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CohortResponse(
    UUID         id,
    String       title,
    String       description,
    LocalDate    startDate,
    LocalDate    endDate,
    int          totalWeeks,
    CohortFormat format,
    CohortStatus status,
    Boolean      registrationOpen,
    Instant      createdAt
) {}
