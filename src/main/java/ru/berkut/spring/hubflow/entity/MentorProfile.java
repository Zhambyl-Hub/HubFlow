package ru.berkut.spring.hubflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Профиль ментора — один на пользователя, независимо от числа когорт.
 * Создаётся при первом назначении роли MENTOR в любой когорте.
 */
@Entity
@Table(name = "mentor_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // "Product, Growth, Fundraising"
    @Column(columnDefinition = "TEXT")
    private String expertise;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
