package ru.berkut.spring.hubflow.web.controller;

import org.springframework.web.bind.annotation.*;
import ru.berkut.spring.hubflow.entity.MentorBooking;
import ru.berkut.spring.hubflow.entity.MentorSlot;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.MentorService;
import ru.berkut.spring.hubflow.web.dto.request.BookSlotRequest;
import ru.berkut.spring.hubflow.web.dto.request.CreateSlotRequest;
import ru.berkut.spring.hubflow.web.dto.request.LeaveFeedbackRequest;
import ru.berkut.spring.hubflow.web.dto.response.MentorBookingResponse;
import ru.berkut.spring.hubflow.web.dto.response.MentorSlotResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cohorts/{cohortId}/mentor-slots")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    // GET /hubflow/api/v1/cohorts/{cohortId}/mentor-slots — свободные слоты
    @GetMapping
    public ResponseEntity<List<MentorSlotResponse>> getFreeSlots(
            @PathVariable UUID cohortId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(mentorService.getFreeSlots(cohortId, principal)
            .stream().map(this::toSlotResponse).toList());
    }

    // POST /hubflow/api/v1/cohorts/{cohortId}/mentor-slots — создать слот (MENTOR/ADMIN)
    @PostMapping
    public ResponseEntity<MentorSlotResponse> createSlot(
            @PathVariable UUID cohortId,
            @Valid @RequestBody CreateSlotRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        MentorSlot slot = mentorService.createSlot(
            new MentorService.CreateSlotRequest(cohortId, req.slotStart(), req.durationMinutes()),
            principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSlotResponse(slot));
    }

    // POST /hubflow/api/v1/cohorts/{cohortId}/mentor-slots/{slotId}/book
    @PostMapping("/{slotId}/book")
    public ResponseEntity<MentorBookingResponse> book(
            @PathVariable UUID cohortId,
            @PathVariable UUID slotId,
            @Valid @RequestBody BookSlotRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        MentorBooking booking = mentorService.book(
            new MentorService.BookSlotRequest(slotId, req.teamId(), req.notes()), principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toBookingResponse(booking));
    }
    //PATCH /mentor-slots/{slotId}/cancel
    @PatchMapping("/{slotId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID cohortId,
            @PathVariable UUID slotId,
            @AuthenticationPrincipal UserPrincipal principal
    ){
        mentorService.cancelSlot(cohortId,slotId,principal);
        return ResponseEntity.noContent().build();
    }

    // DELETE /hubflow/api/v1/cohorts/{cohortId}/mentor-slots/bookings/{bookingId}
    @PatchMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable UUID cohortId,
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        mentorService.cancelBooking(bookingId, principal);
        return ResponseEntity.noContent().build();
    }

    // POST /hubflow/api/v1/cohorts/{cohortId}/mentor-slots/bookings/{bookingId}/feedback
    @PostMapping("/bookings/{bookingId}/feedback")
    public ResponseEntity<Void> leaveFeedback(
            @PathVariable UUID cohortId,
            @PathVariable UUID bookingId,
            @Valid @RequestBody LeaveFeedbackRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        mentorService.leaveFeedback(
            new MentorService.FeedbackRequest(bookingId, req.content(), req.readinessScore()),
            principal);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private MentorSlotResponse toSlotResponse(MentorSlot s) {
        return new MentorSlotResponse(s.getId(),
            s.getMentor().getId(), s.getMentor().getFirstName(), s.getMentor().getLastName(),
            s.getSlotStart(), s.getDurationMinutes(), s.getStatus());
    }

    private MentorBookingResponse toBookingResponse(MentorBooking b) {
        MentorSlot slot = b.getMentorSlot();
        return new MentorBookingResponse(b.getId(),
            slot.getId(), slot.getSlotStart(), slot.getDurationMinutes(),
            slot.getMentor().getId(), slot.getMentor().getFirstName(), slot.getMentor().getLastName(),
            b.getTeam().getId(), b.getTeam().getName(),
            b.getNotes(), b.getStatus(), b.getBookedAt());
    }
}
