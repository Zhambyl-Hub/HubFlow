package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berkut.spring.hubflow.entity.Checkpoint;

import java.util.List;
import java.util.UUID;

public interface CheckpointRepository extends JpaRepository<Checkpoint, UUID> {
    List<Checkpoint> findByWeekIdOrderByOrderIndex(UUID weekId);

    @Query("SELECT c FROM Checkpoint c WHERE c.week.cohort.id = :cohortId ORDER BY c.week.weekNumber, c.orderIndex")
    List<Checkpoint> findAllByCohortId(UUID cohortId);

}
