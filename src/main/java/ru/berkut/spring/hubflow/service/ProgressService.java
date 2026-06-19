package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.Checkpoint;
import ru.berkut.spring.hubflow.entity.CheckpointProgress;
import ru.berkut.spring.hubflow.entity.Team;
import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.enums.ProgressStatus;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.CheckpointProgressRepository;
import ru.berkut.spring.hubflow.repository.CheckpointRepository;
import ru.berkut.spring.hubflow.repository.TeamRepository;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berkut.spring.hubflow.web.dto.response.CheckpointStatusResponse;
import ru.berkut.spring.hubflow.web.dto.response.CheckpointSummaryResponse;
import ru.berkut.spring.hubflow.web.dto.response.ProgressMatrixResponse;
import ru.berkut.spring.hubflow.web.dto.response.TeamProgressResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final CheckpointProgressRepository progressRepository;
    private final CheckpointRepository         checkpointRepository;
    private final TeamRepository               teamRepository;
    private final TeamService                  teamService;
    private final CohortService                cohortService;

    public record MarkRequest(UUID teamId, UUID checkpointId,
                              ProgressStatus status, String proofUrl, String comment) {}

    @Transactional
    public CheckpointProgress mark(MarkRequest req, UserPrincipal principal) {
        teamService.requireTeamMember(principal.getId(), req.teamId());

        Checkpoint checkpoint = checkpointRepository.findById(req.checkpointId())
                .orElseThrow(() -> NotFoundException.of("Checkpoint", req.checkpointId()));
        Team team = teamRepository.getReferenceById(req.teamId());
        User completedBy = new User();
        completedBy.setId(principal.getId()); // proxy reference

        CheckpointProgress progress = progressRepository
                .findByCheckpointIdAndTeamId(req.checkpointId(), req.teamId())
                .orElseGet(() -> CheckpointProgress.builder()
                        .checkpoint(checkpoint)
                        .team(team)
                        .build());

        progress.setStatus(req.status());
        progress.setProofUrl(req.proofUrl());
        progress.setComment(req.comment());
        progress.setCompletedAt(Instant.now());

        return progressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public ProgressMatrixResponse getMatrix(UUID cohortId , UserPrincipal principal) {
        cohortService.requireAdminOrMentor(principal.getId(), cohortId);
        // 1. Все чекпоинты
        List<Checkpoint> allCheckpoints = checkpointRepository.findAllByCohortId(cohortId);
        // 2. Все APPROVED команды когорты
        List<Team> approvedTeams = teamRepository.findApprovedTeamsByCohortId(cohortId);
        // 3. Существующие записи прогресса
        List<CheckpointProgress> existingProgress = progressRepository.findAllByCohortId(cohortId);

        // Быстрый доступ к существующей записи по ключу "teamId::checkpointId"
        Map<String, CheckpointProgress> progressByKey = existingProgress.stream()
                .collect(Collectors.toMap(
                        p -> matrixKey(p.getTeam().getId(), p.getCheckpoint().getId()),
                        p -> p,
                        (a, b) -> a // по уникальному constraint дублей не будет
                ));

        List<CheckpointSummaryResponse> checkpoints = allCheckpoints.stream()
                .map(cp -> new CheckpointSummaryResponse(
                        cp.getId(),
                        cp.getTitle(),
                        cp.getWeek().getWeekNumber()
                ))
                .toList();

        // 4-7. Каждая команда несёт свой полный список статусов по всем чекпоинтам когорты
        // (для отсутствующих записей — NOT_STARTED), прогресс считается по этому же списку.
        List<TeamProgressResponse> teams = approvedTeams.stream()
                .map(team -> toTeamProgressResponse(team, allCheckpoints, progressByKey))
                .toList();

        return new ProgressMatrixResponse(teams, checkpoints);
    }

    private String matrixKey(UUID teamId, UUID checkpointId) {
        return teamId + "::" + checkpointId;
    }

    @Transactional(readOnly = true)
    public double getTeamProgress(UUID teamId) {
        List<CheckpointProgress> list = progressRepository.findByTeamId(teamId);
        if (list.isEmpty()) return 0.0;
        long done = list.stream()
                .filter(p -> p.getStatus() == ProgressStatus.DONE).count();
        return (double) done / list.size() * 100;
    }

    /**
     * Собирает статус по каждому чекпоинту когорты для команды (NOT_STARTED, если записи нет)
     * и считает её общий прогресс по всем чекпоинтам когорты — даже если ни одной записи нет,
     * результат будет 0%, а не отсутствие команды в ответе.
     */
    private TeamProgressResponse toTeamProgressResponse(
            Team team,
            List<Checkpoint> allCheckpoints,
            Map<String, CheckpointProgress> progressByKey
    ) {
        List<CheckpointStatusResponse> checkpointStatuses = allCheckpoints.stream()
                .map(checkpoint -> {
                    CheckpointProgress existing = progressByKey.get(matrixKey(team.getId(), checkpoint.getId()));
                    ProgressStatus status = existing != null ? existing.getStatus() : ProgressStatus.NOT_STARTED;
                    return new CheckpointStatusResponse(checkpoint.getId(), status);
                })
                .toList();

        long done = checkpointStatuses.stream()
                .filter(cs -> cs.status() == ProgressStatus.DONE)
                .count();
        double progress = allCheckpoints.isEmpty() ? 0.0 : (done * 100.0) / allCheckpoints.size();

        return new TeamProgressResponse(team.getId(), team.getName(), progress, checkpointStatuses);
    }
}