package ru.berkut.spring.hubflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "demo_day_participants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"demo_day_id", "team_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DemoDayParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demo_day_id", nullable = false)
    private DemoDay demoDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "presentation_order")
    private Integer presentationOrder;

    @Column(name = "pitch_deck_url", length = 500)
    private String pitchDeckUrl;

    @Column(name = "video_url", length = 500)
    private String videoUrl;
}
