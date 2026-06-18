package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berkut.spring.hubflow.entity.Vote;

import java.util.List;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    List<Vote> findByDemoDayId(UUID demoDayId);
    boolean existsByDemoDayIdAndVoterIdAndTeamIdAndCriterionId(
        UUID demoDayId, UUID voterId, UUID teamId, UUID criterionId);

    // Итоговый рейтинг: сумма баллов по командам
    @Query("""
        SELECT v.team.id, SUM(v.score)
        FROM Vote v
        WHERE v.demoDay.id = :demoDayId
        GROUP BY v.team.id
        ORDER BY SUM(v.score) DESC
    """)
    List<Object[]> getScoresByDemoDay(UUID demoDayId);
}
