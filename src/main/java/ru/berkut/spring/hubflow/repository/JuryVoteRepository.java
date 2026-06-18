package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berkut.spring.hubflow.entity.JuryVote;

import java.util.List;
import java.util.UUID;

public interface JuryVoteRepository extends JpaRepository<JuryVote, UUID> {

    List<JuryVote> findByDemoDayIdAndJuryId(UUID demoDayId, UUID juryId);

    boolean existsByDemoDayIdAndJuryIdAndTeamIdAndCriterionId(
            UUID demoDayId, UUID juryId, UUID teamId, UUID criterionId);

    // Итоговый рейтинг: сумма баллов по командам
    @Query("""
        SELECT v.team.id, v.team.name, CAST(SUM(v.score) AS long)
        FROM JuryVote v
        WHERE v.demoDay.id = :demoDayId
        GROUP BY v.team.id, v.team.name
        ORDER BY SUM(v.score) DESC
    """)
    List<Object[]> getRankingByDemoDay(UUID demoDayId);

    // Детальная разбивка по критериям для одной команды
    @Query("""
        SELECT v.criterion.title, CAST(SUM(v.score) AS long), CAST(COUNT(v) AS long)
        FROM JuryVote v
        WHERE v.demoDay.id = :demoDayId AND v.team.id = :teamId
        GROUP BY v.criterion.id, v.criterion.title, v.criterion.orderIndex
        ORDER BY v.criterion.orderIndex
    """)
    List<Object[]> getCriteriaBreakdown(UUID demoDayId, UUID teamId);

    // Проверка: все ли команды оценены данным членом жюри
    @Query("""
        SELECT COUNT(DISTINCT v.team.id)
        FROM JuryVote v
        WHERE v.demoDay.id = :demoDayId AND v.jury.id = :juryId
    """)
    long countDistinctTeamsVotedByJury(UUID demoDayId, UUID juryId);
}