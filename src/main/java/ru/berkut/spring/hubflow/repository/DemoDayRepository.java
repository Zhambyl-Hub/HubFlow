package ru.berkut.spring.hubflow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.DemoDay;

import java.util.Optional;
import java.util.UUID;

public interface DemoDayRepository extends JpaRepository<DemoDay, UUID> {
    Optional<DemoDay> findByCohortId(UUID cohortId);
}
