package ru.berkut.spring.hubflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audience_likes",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"demo_day_id", "user_id", "team_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AudienceLike {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demo_day_id", nullable = false)
    private DemoDay demoDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "liked_at")
    @Builder.Default
    private Instant likedAt = Instant.now();
}