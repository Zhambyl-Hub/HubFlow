package ru.berkut.spring.hubflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accountability_pairs",
    uniqueConstraints = @UniqueConstraint(columnNames = {"cohort_id", "team_a_id", "team_b_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountabilityPair {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_a_id", nullable = false)
    private Team teamA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_b_id", nullable = false)
    private Team teamB;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
