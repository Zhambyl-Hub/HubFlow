package ru.berkut.spring.hubflow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.CohortMembership;
import ru.berkut.spring.hubflow.enums.MembershipRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CohortMembershipRepository extends JpaRepository<CohortMembership, UUID> {
    Optional<CohortMembership> findByUserIdAndCohortId(UUID userId, UUID cohortId);
    List<CohortMembership> findByCohortId(UUID cohortId);
    List<CohortMembership> findByUserId(UUID userId);
    List<CohortMembership> findByCohortIdAndRole(UUID cohortId, MembershipRole role);
    List<CohortMembership> findByUserIdAndRole(UUID userId, MembershipRole role);
    boolean existsByUserIdAndCohortId(UUID userId, UUID cohortId);
}
