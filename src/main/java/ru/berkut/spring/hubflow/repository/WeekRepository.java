package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.Week;

import java.util.List;
import java.util.UUID;

public interface WeekRepository extends JpaRepository<Week, UUID> {
    List<Week> findByCohortIdOrderByWeekNumber(UUID cohortId);
}
