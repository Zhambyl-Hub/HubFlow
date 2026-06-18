package ru.berkut.spring.hubflow.entity;

import ru.berkut.spring.hubflow.enums.MembershipRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cohort_memberships",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cohort_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CohortMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipRole role;

    @Column(name = "joined_at")
    @Builder.Default
    private Instant joinedAt = Instant.now();
}
