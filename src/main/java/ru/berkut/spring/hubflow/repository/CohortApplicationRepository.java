package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berkut.spring.hubflow.entity.CohortApplication;
import ru.berkut.spring.hubflow.enums.ApplicationStatus;
import ru.berkut.spring.hubflow.enums.CohortApplicationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CohortApplicationRepository extends JpaRepository<CohortApplication, UUID> {
    List<CohortApplication> findByCohortId(UUID cohortId);
    List<CohortApplication> findByCohortIdAndStatus(UUID cohortId, CohortApplicationStatus status);
    Optional<CohortApplication> findByTeamIdAndCohortId(UUID teamId, UUID cohortId);
    List<CohortApplication> findByTeamId(UUID teamId);

    Optional<CohortApplication> findByCohortIdAndTeamId(UUID cohortId, UUID teamId);
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM CohortApplication a
        WHERE a.team.id = :teamId AND a.cohort.id = :cohortId
              AND a.status = 'APPROVED'
    """)
    boolean isTeamApprovedInCohort(UUID teamId, UUID cohortId);
}
