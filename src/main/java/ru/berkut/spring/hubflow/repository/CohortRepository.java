package ru.berkut.spring.hubflow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.Cohort;
import ru.berkut.spring.hubflow.enums.CohortStatus;
import ru.berkut.spring.hubflow.web.dto.request.UpdateRegistrationRequest;

import java.util.List;
import java.util.UUID;

public interface CohortRepository extends JpaRepository<Cohort, UUID> {
    List<Cohort> findByStatus(CohortStatus status);
    List<Cohort> findByCreatedById(UUID userId);
    List<Cohort> findByRegistrationOpenTrue();
}
