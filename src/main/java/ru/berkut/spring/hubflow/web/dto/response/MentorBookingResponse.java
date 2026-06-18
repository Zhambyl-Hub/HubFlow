package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.BookingStatus;
import java.time.Instant;
import java.util.UUID;

public record MentorBookingResponse(
    UUID          id,
    UUID          slotId,
    Instant       slotStart,
    int           durationMinutes,
    UUID          mentorId,
    String        mentorFirstName,
    String        mentorLastName,
    UUID          teamId,
    String        teamName,
    String        notes,
    BookingStatus status,
    Instant       bookedAt
) {}
