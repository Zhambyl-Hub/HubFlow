package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berkut.spring.hubflow.entity.AccountabilityPair;

import java.util.List;
import java.util.UUID;

public interface AccountabilityPairRepository extends JpaRepository<AccountabilityPair, UUID> {
    List<AccountabilityPair> findByCohortId(UUID cohortId);

    @Query("SELECT p FROM AccountabilityPair p WHERE p.teamA.id = :teamId OR p.teamB.id = :teamId")
    List<AccountabilityPair> findByTeamId(UUID teamId);
}
