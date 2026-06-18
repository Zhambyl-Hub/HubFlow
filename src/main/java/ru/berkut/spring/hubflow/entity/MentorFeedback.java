package ru.berkut.spring.hubflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mentor_feedback")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // один фидбэк на одну сессию
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private MentorBooking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // оценка готовности команды: 1..5
    @Column(name = "readiness_score")
    private Integer readinessScore;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
