package ru.berkut.spring.hubflow.entity;

import ru.berkut.spring.hubflow.enums.TeamStage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Команда — самостоятельная сущность, не привязана к когорте напрямую.
 * Связь с когортой устанавливается через CohortApplication (после APPROVED).
 */
@Entity
@Table(name = "teams")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "idea_description", columnDefinition = "TEXT")
    private String ideaDescription;

    @Column(columnDefinition = "TEXT")
    private String problem;

    @Column(name = "target_segment", length = 255)
    private String targetSegment;

    @Column(columnDefinition = "TEXT")
    private String solution;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TeamStage stage;

    @Column(name = "repo_url", length = 500)
    private String repoUrl;

    @Column(name = "landing_url", length = 500)
    private String landingUrl;

    @Column(name = "pitch_url", length = 500)
    private String pitchUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
