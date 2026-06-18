package ru.berkut.spring.hubflow.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.berkut.spring.hubflow.enums.SystemRole;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_role_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemRoleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_role", length = 20)
    private SystemRole oldRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_role", nullable = false, length = 20)
    private SystemRole newRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "changed_at")
    @Builder.Default
    private Instant changedAt = Instant.now();
}
