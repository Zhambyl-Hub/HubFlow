package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berkut.spring.hubflow.entity.AudienceLike;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AudienceLikeRepository extends JpaRepository<AudienceLike, UUID> {

    Optional<AudienceLike> findByDemoDayIdAndUserIdAndTeamId(UUID demoDayId, UUID userId, UUID teamId);

    boolean existsByDemoDayIdAndUserIdAndTeamId(UUID demoDayId, UUID userId, UUID teamId);

    // Количество лайков по каждой команде
    @Query("""
        SELECT l.team.id, l.team.name, CAST(COUNT(l) AS long)
        FROM AudienceLike l
        WHERE l.demoDay.id = :demoDayId
        GROUP BY l.team.id, l.team.name
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> getLikeCountsByDemoDay(UUID demoDayId);
}