package ru.berkut.spring.hubflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "demo_criteria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DemoCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demo_day_id", nullable = false)
    private DemoDay demoDay;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_score", nullable = false)
    @Builder.Default
    private Integer maxScore = 10;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}
