package ru.berkut.spring.hubflow.entity;

import ru.berkut.spring.hubflow.enums.PeerReviewStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "peer_reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PeerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accountability_pair_id", nullable = false)
    private AccountabilityPair accountabilityPair;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_team_id", nullable = false)
    private Team reviewerTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewed_team_id", nullable = false)
    private Team reviewedTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    private Checkpoint checkpoint;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PeerReviewStatus status;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;
}
