package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.SlotStatus;
import java.time.Instant;
import java.util.UUID;

public record MentorSlotResponse(
    UUID       id,
    UUID       mentorId,
    String     mentorFirstName,
    String     mentorLastName,
    Instant    slotStart,
    int        durationMinutes,
    SlotStatus status
) {}
