package ru.berkut.spring.hubflow.entity;

import ru.berkut.spring.hubflow.enums.VotingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "demo_days")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DemoDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // одна когорта — один Demo Day
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false, unique = true)
    private Cohort cohort;

    @Column(name = "event_date", nullable = false)
    private Instant eventDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "voting_status", nullable = false, length = 20)
    @Builder.Default
    private VotingStatus votingStatus = VotingStatus.CLOSED;

    @Column(name = "show_results_publicly", nullable = false)
    @Builder.Default
    private Boolean showResultsPublicly = false;

    @Column(name = "voting_opens_at")
    private Instant votingOpensAt;

    @Column(name = "voting_closes_at")
    private Instant votingClosesAt;
}
