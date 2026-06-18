package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.DemoDay;
import ru.berkut.spring.hubflow.entity.Team;
import ru.berkut.spring.hubflow.enums.VotingStatus;
import ru.berkut.spring.hubflow.exception.AccessDeniedException;
import ru.berkut.spring.hubflow.exception.BadRequestException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.*;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berkut.spring.hubflow.web.dto.CriteriaScore;
import ru.berkut.spring.hubflow.web.dto.TeamResult;
import ru.berkut.spring.hubflow.web.dto.response.RankingResponse;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final DemoDayRepository      demoDayRepository;
    private final JuryVoteRepository     juryVoteRepository;
    private final AudienceLikeRepository audienceLikeRepository;
    private final TeamRepository         teamRepository;
    private final CohortService          cohortService;

    // ─────────────────── Response records ───────────────────






    // ─────────────────── Полный рейтинг ───────────────────

    /**
     * Полный рейтинг с разбивкой по критериям.
     * Доступен: ADMIN всегда, остальные — только если showResultsPublicly=true.
     */
    @Transactional(readOnly = true)
    public RankingResponse getFullRanking(UUID demoDayId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        checkResultsAccess(day, principal);

        List<Object[]> rawRanking = juryVoteRepository.getRankingByDemoDay(demoDayId);
        Map<UUID, Long> likeCounts = getLikeCountsMap(demoDayId);

        List<TeamResult> results = new ArrayList<>();
        for (int i = 0; i < rawRanking.size(); i++) {
            Object[] row      = rawRanking.get(i);
            UUID     teamId   = (UUID)   row[0];
            String   teamName = (String) row[1];
            long     total    = ((Number) row[2]).longValue();

            List<CriteriaScore> breakdown = buildBreakdown(demoDayId, teamId);
            long likes = likeCounts.getOrDefault(teamId, 0L);

            results.add(new TeamResult(i + 1, teamId, teamName, total, likes, breakdown));
        }

        // Добавляем команды без голосов (0 баллов) — чтобы видеть всех участников
        addTeamsWithNoVotes(demoDayId, results, likeCounts);

        return new RankingResponse(demoDayId, day.getVotingStatus(), results);
    }

    // ─────────────────── Топ-3 ───────────────────

    @Transactional(readOnly = true)
    public List<TeamResult> getTop3(UUID demoDayId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        checkResultsAccess(day, principal);

        List<Object[]> rawRanking = juryVoteRepository.getRankingByDemoDay(demoDayId);
        Map<UUID, Long> likeCounts = getLikeCountsMap(demoDayId);

        return rawRanking.stream()
                .limit(3)
                .map(row -> {
                    UUID   teamId   = (UUID)   row[0];
                    String teamName = (String) row[1];
                    long   total    = ((Number) row[2]).longValue();
                    int    rank     = rawRanking.indexOf(row) + 1;
                    return new TeamResult(rank, teamId, teamName, total,
                            likeCounts.getOrDefault(teamId, 0L),
                            buildBreakdown(demoDayId, teamId));
                })
                .toList();
    }

    // ─────────────────── Победитель ───────────────────

    @Transactional(readOnly = true)
    public TeamResult getWinner(UUID demoDayId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);

        if (day.getVotingStatus() != VotingStatus.FINISHED) {
            throw new BadRequestException("Победитель определяется только после закрытия голосования");
        }
        checkResultsAccess(day, principal);

        List<Object[]> ranking = juryVoteRepository.getRankingByDemoDay(demoDayId);
        if (ranking.isEmpty()) {
            throw new NotFoundException("Нет голосов — победитель не определён");
        }

        Object[] top  = ranking.get(0);
        UUID     teamId   = (UUID)   top[0];
        String   teamName = (String) top[1];
        long     total    = ((Number) top[2]).longValue();
        Map<UUID, Long> likes = getLikeCountsMap(demoDayId);

        return new TeamResult(1, teamId, teamName, total,
                likes.getOrDefault(teamId, 0L),
                buildBreakdown(demoDayId, teamId));
    }

    // ─────────────────── Детальная разбивка одной команды ───────────────────

    @Transactional(readOnly = true)
    public List<CriteriaScore> getTeamBreakdown(UUID demoDayId, UUID teamId,
                                                UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        checkResultsAccess(day, principal);
        return buildBreakdown(demoDayId, teamId);
    }

    // ─────────────────── helpers ───────────────────

    private List<CriteriaScore> buildBreakdown(UUID demoDayId, UUID teamId) {
        return juryVoteRepository.getCriteriaBreakdown(demoDayId, teamId)
                .stream()
                .map(row -> {
                    String title      = (String) row[0];
                    long   totalScore = ((Number) row[1]).longValue();
                    long   voters     = ((Number) row[2]).longValue();
                    double avg        = voters > 0 ? (double) totalScore / voters : 0.0;
                    return new CriteriaScore(title, totalScore, voters, avg);
                })
                .toList();
    }

    private Map<UUID, Long> getLikeCountsMap(UUID demoDayId) {
        Map<UUID, Long> map = new HashMap<>();
        audienceLikeRepository.getLikeCountsByDemoDay(demoDayId)
                .forEach(row -> map.put((UUID) row[0], ((Number) row[2]).longValue()));
        return map;
    }

    /**
     * Участники Demo Day без голосов попадают в конец рейтинга с 0 баллами.
     */
    private void addTeamsWithNoVotes(UUID demoDayId, List<TeamResult> results,
                                     Map<UUID, Long> likeCounts) {
        Set<UUID> alreadyRanked = new HashSet<>();
        results.forEach(r -> alreadyRanked.add(r.teamId()));

        teamRepository.findByDemoDayId(demoDayId).forEach(team -> {
            if (!alreadyRanked.contains(team.getId())) {
                results.add(new TeamResult(
                        results.size() + 1,
                        team.getId(),
                        team.getName(),
                        0L,
                        likeCounts.getOrDefault(team.getId(), 0L),
                        List.of()
                ));
            }
        });
    }

    private DemoDay getDemoDay(UUID id) {
        return demoDayRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("DemoDay", id));
    }

    private void checkResultsAccess(DemoDay day, UserPrincipal principal) {
        boolean isAdmin = cohortService.isAdmin(principal.getId(), day.getCohort().getId());
        if (!isAdmin && !day.getShowResultsPublicly()) {
            throw new AccessDeniedException("Результаты ещё не опубликованы");
        }
    }
}