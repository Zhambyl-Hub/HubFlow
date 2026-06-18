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

    public record MarkRequest(UUID teamId, UUID checkpointId,
                              ProgressStatus status, String proofUrl, String comment) {}

    // Матрица прогресса для дашборда: teamId -> checkpointId -> status
    public record ProgressMatrix(List<Team> teams,
                                 List<Checkpoint> checkpoints,
                                 Map<String, ProgressStatus> matrix) {}

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
    public ProgressMatrixResponse getMatrix(UUID cohortId) {
        List<CheckpointProgress> allProgress = progressRepository.findAllByCohortId(cohortId);
        List<CheckpointSummaryResponse> checkpoints =
                allProgress.stream()
                        .map(CheckpointProgress::getCheckpoint)
                        .distinct()
                        .map(cp -> new CheckpointSummaryResponse(
                                cp.getId(),
                                cp.getTitle(),
                                cp.getWeek().getWeekNumber()
                        ))
                        .toList();
        List<TeamProgressResponse> teams =
                allProgress.stream()
                        .map(CheckpointProgress::getTeam)
                        .distinct()
                        .map(team -> new TeamProgressResponse(
                                team.getId(),
                                team.getName(),
                                calculateTeamProgress(team, allProgress, checkpoints.size())
                        ))
                        .toList();


        // Ключ: "teamId::checkpointId"
        Map<String, ProgressStatus> matrix = allProgress.stream()
            .collect(Collectors.toMap(
                p -> p.getTeam().getId() + "::" + p.getCheckpoint().getId(),
                CheckpointProgress::getStatus
            ));
        return new ProgressMatrixResponse (teams, checkpoints, matrix);
    }

    @Transactional(readOnly = true)
    public double getTeamProgress(UUID teamId) {
        List<CheckpointProgress> list = progressRepository.findByTeamId(teamId);
        if (list.isEmpty()) return 0.0;
        long done = list.stream()
            .filter(p -> p.getStatus() == ProgressStatus.DONE).count();
        return (double) done / list.size() * 100;
    }
    private double calculateTeamProgress(
            Team team,
            List<CheckpointProgress> allProgress,
            int totalCheckpoints
    ) {
        long done = allProgress.stream()
                .filter(p -> p.getTeam().getId().equals(team.getId()))
                .filter(p -> p.getStatus() == ProgressStatus.DONE)
                .count();

        if (totalCheckpoints == 0) return 0.0;

        return (done * 100.0) / totalCheckpoints;
    }
}
