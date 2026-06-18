package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.Cohort;
import ru.berkut.spring.hubflow.entity.DemoCriteria;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DemoCriteriaRepository extends JpaRepository<DemoCriteria,UUID> {
    DemoCriteria findDemoCriteriaById(UUID id);
    List<DemoCriteria> findByDemoDayIdOrderByOrderIndex(UUID demoDayId);
}
