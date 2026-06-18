package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berkut.spring.hubflow.entity.CheckpointProgress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckpointProgressRepository extends JpaRepository<CheckpointProgress, UUID> {
    Optional<CheckpointProgress> findByCheckpointIdAndTeamId(UUID checkpointId, UUID teamId);
    List<CheckpointProgress> findByTeamId(UUID teamId);

    // Матрица прогресса: все записи по когорте
    @Query("""
        SELECT cp FROM CheckpointProgress cp
        WHERE cp.checkpoint.week.cohort.id = :cohortId
              AND EXISTS (
                  SELECT 1 FROM CohortApplication a
                  WHERE a.team.id = cp.team.id
                        AND a.cohort.id = :cohortId
                        AND a.status = 'APPROVED'
              )
    """)
    List<CheckpointProgress> findAllByCohortId(UUID cohortId);
}
