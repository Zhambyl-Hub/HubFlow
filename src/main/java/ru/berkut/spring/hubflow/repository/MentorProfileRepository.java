package ru.berkut.spring.hubflow.repository;

import ru.berkut.spring.hubflow.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID> {
    Optional<MentorProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    // Каталог менторов — только видимые
    List<MentorProfile> findByIsVisibleTrue();

    // Менторы конкретной когорты (через membership)
    @Query("""
        SELECT mp FROM MentorProfile mp
        WHERE mp.user.id IN (
            SELECT m.user.id FROM CohortMembership m
            WHERE m.cohort.id = :cohortId
              AND m.role = 'MENTOR'
        )
        AND mp.isVisible = true
    """)
    List<MentorProfile> findByCohortId(UUID cohortId);
}
