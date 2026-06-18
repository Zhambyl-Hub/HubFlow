package ru.berkut.spring.hubflow.service;

import org.springframework.http.ResponseEntity;
import ru.berkut.spring.hubflow.entity.*;
import ru.berkut.spring.hubflow.enums.BookingStatus;
import ru.berkut.spring.hubflow.enums.NotificationType;
import ru.berkut.spring.hubflow.enums.SlotStatus;
import ru.berkut.spring.hubflow.exception.AccessDeniedException;
import ru.berkut.spring.hubflow.exception.BadRequestException;
import ru.berkut.spring.hubflow.exception.ConflictException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.*;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorSlotRepository    slotRepository;
    private final MentorBookingRepository bookingRepository;
    private final UserRepository          userRepository;
    private final CohortRepository        cohortRepository;
    private final TeamRepository          teamRepository;
    private final CohortService           cohortService;
    private final TeamService             teamService;
    private final NotificationService     notificationService;
    private final MentorFeedbackRepository feedbackRepository;


    public record CreateSlotRequest(UUID cohortId, Instant slotStart, int durationMinutes) {}
    public record BookSlotRequest(UUID slotId, UUID teamId, String notes) {}
    public record FeedbackRequest(UUID bookingId, String content, int readinessScore) {}

    // ── Слоты ────────────────────────────────────────────────────────────

    @Transactional
    public MentorSlot createSlot(CreateSlotRequest req, UserPrincipal principal) {
        cohortService.requireMentor(principal.getId(), req.cohortId());
        User mentor   = userRepository.getReferenceById(principal.getId());
        Cohort cohort = cohortRepository.getReferenceById(req.cohortId());

        return slotRepository.save(MentorSlot.builder()
            .mentor(mentor)
            .cohort(cohort)
            .slotStart(req.slotStart())
            .durationMinutes(req.durationMinutes())
            .status(SlotStatus.FREE)
            .build());
    }
    @Transactional
    public void cancelSlot(UUID cohortId,UUID slotId, UserPrincipal principal) {
        cohortService.requireMentor(principal.getId(), cohortId);
        MentorSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> NotFoundException.of("MentorSlot", slotId));
        if (!slot.getMentor().getId().equals(principal.getId())) {
            throw new AccessDeniedException(
                    "You can cancel only your own slots"
            );
        }
        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new ConflictException(
                    "Cannot cancel booked slot"
            );
        }
        slot.setStatus(SlotStatus.CANCELLED);
    }
    @Transactional(readOnly = true)
    public List<MentorSlot> getFreeSlots(UUID cohortId, UserPrincipal principal) {
        cohortService.checkMembership(principal.getId(), cohortId);
        return slotRepository.findByCohortIdAndStatus(cohortId, SlotStatus.FREE);
    }

    // ── Бронирование ─────────────────────────────────────────────────────

    @Transactional
    public MentorBooking book(BookSlotRequest req, UserPrincipal principal) {
        teamService.requireTeamLead(principal.getId(), req.teamId());

        MentorSlot slot = slotRepository.findById(req.slotId())
            .orElseThrow(() -> NotFoundException.of("MentorSlot", req.slotId()));

        if (slot.getStatus() != SlotStatus.FREE) {
            throw new ConflictException("Slot is not available");
        }

        Team team = teamRepository.getReferenceById(req.teamId());
        MentorBooking booking = bookingRepository.save(MentorBooking.builder()
            .mentorSlot(slot)
            .team(team)
            .notes(req.notes())
            .status(BookingStatus.CONFIRMED)
            .build());

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        // Уведомление ментору
        notificationService.send(
            slot.getMentor().getId(),
            NotificationType.SESSION_BOOKED,
            "Новая бронь",
            "Команда «" + team.getName() + "» забронировала сессию",
            null
        );
        return booking;
    }

    @Transactional
    public void cancelBooking(UUID bookingId, UserPrincipal principal) {
        MentorBooking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> NotFoundException.of("Booking", bookingId));

        UUID teamId = booking.getTeam().getId();
        teamService.requireTeamLead(principal.getId(), teamId);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.getMentorSlot().setStatus(SlotStatus.FREE);
        bookingRepository.save(booking);
    }

    // ── Фидбэк ───────────────────────────────────────────────────────────

    @Transactional
    public MentorFeedback leaveFeedback(FeedbackRequest req, UserPrincipal principal) {
        MentorBooking booking = bookingRepository.findById(req.bookingId())
            .orElseThrow(() -> NotFoundException.of("Booking", req.bookingId()));

        if (!booking.getMentorSlot().getMentor().getId().equals(principal.getId())) {
            throw new AccessDeniedException("Only the mentor can leave feedback");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Feedback only for confirmed sessions");
        }

        User mentor = userRepository.getReferenceById(principal.getId());
        MentorFeedback feedback = MentorFeedback.builder()
            .booking(booking)
            .mentor(mentor)
            .team(booking.getTeam())
            .content(req.content())
            .readinessScore(req.readinessScore())
            .build();
        feedbackRepository.save(feedback);
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        // Уведомление команде
        notificationService.send(
            booking.getTeam().getCreatedBy().getId(),
            NotificationType.MENTOR_FEEDBACK,
            "Новый фидбэк от ментора",
            req.content().substring(0, Math.min(req.content().length(), 100)),
            null
        );

        return feedback;
    }
}
