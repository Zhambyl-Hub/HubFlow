package ru.berkut.spring.hubflow.entity;

import ru.berkut.spring.hubflow.enums.JuryInviteStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jury_invites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JuryInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demo_day_id", nullable = false)
    private DemoDay demoDay;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(name = "invite_token", nullable = false, unique = true, length = 128)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private JuryInviteStatus status = JuryInviteStatus.PENDING;

    // заполняется после активации
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jury_user_id")
    private User juryUser;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}