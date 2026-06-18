package ru.berkut.spring.hubflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jury_votes",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"demo_day_id", "jury_id", "team_id", "criterion_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JuryVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demo_day_id", nullable = false)
    private DemoDay demoDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jury_id", nullable = false)
    private User jury;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criterion_id", nullable = false)
    private DemoCriteria criterion;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "voted_at")
    @Builder.Default
    private Instant votedAt = Instant.now();
}