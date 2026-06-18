package ru.berkut.spring.hubflow.entity;

import ru.berkut.spring.hubflow.enums.CohortApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cohort_applications",
    uniqueConstraints = @UniqueConstraint(columnNames = {"cohort_id", "team_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CohortApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CohortApplicationStatus status = CohortApplicationStatus.PENDING;

    @Column(name = "applied_at")
    @Builder.Default
    private Instant appliedAt = Instant.now();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
}
