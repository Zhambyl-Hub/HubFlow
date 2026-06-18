package ru.berkut.spring.hubflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "weeks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"cohort_id", "week_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Week {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String goal;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "week", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Checkpoint> checkpoints = new ArrayList<>();
}