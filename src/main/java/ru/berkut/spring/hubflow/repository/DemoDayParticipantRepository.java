package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.DemoDayParticipant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DemoDayParticipantRepository extends JpaRepository<DemoDayParticipant, UUID> {
    List<DemoDayParticipant> findByDemoDayIdOrderByPresentationOrder(UUID demoDayId);
    Optional<DemoDayParticipant> findByDemoDayIdAndTeamId(UUID demoDayId, UUID teamId);
    boolean existsByDemoDayIdAndTeamId(UUID demoDayId, UUID teamId);
}
