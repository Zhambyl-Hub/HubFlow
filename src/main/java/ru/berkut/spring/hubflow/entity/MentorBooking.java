package ru.berkut.spring.hubflow.entity;

import ru.berkut.spring.hubflow.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mentor_bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_slot_id", nullable = false)
    private MentorSlot mentorSlot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "booked_at")
    @Builder.Default
    private Instant bookedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;
}
