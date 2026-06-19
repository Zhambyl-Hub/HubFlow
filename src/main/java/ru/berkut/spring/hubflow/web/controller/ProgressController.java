package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.entity.CheckpointProgress;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.ProgressService;
import ru.berkut.spring.hubflow.web.dto.request.MarkProgressRequest;
import ru.berkut.spring.hubflow.web.dto.response.ProgressMatrixResponse;
import ru.berkut.spring.hubflow.web.dto.response.ProgressResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/cohorts/{cohortId}")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // GET /hubflow/api/v1/cohorts/{cohortId}/progress/matrix — матрица для дашборда
    @GetMapping("/progress/matrix")
    public ResponseEntity<ProgressMatrixResponse> getMatrix(
            @PathVariable UUID cohortId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(progressService.getMatrix(cohortId , principal));
    }

    // POST /hubflow/api/v1/cohorts/{cohortId}/teams/{teamId}/checkpoints/{checkpointId}/progress
    @PostMapping("/teams/{teamId}/checkpoints/{checkpointId}/progress")
    public ResponseEntity<ProgressResponse> mark(
            @PathVariable UUID cohortId,
            @PathVariable UUID teamId,
            @PathVariable UUID checkpointId,
            @Valid @RequestBody MarkProgressRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        CheckpointProgress progress = progressService.mark(
            new ProgressService.MarkRequest(teamId, checkpointId,
                req.status(), req.proofUrl(), req.comment()), principal);
        return ResponseEntity.ok(toResponse(progress));
    }

    private ProgressResponse toResponse(CheckpointProgress p) {
        return new ProgressResponse(
            p.getId(),
            p.getCheckpoint().getId(),
            p.getCheckpoint().getTitle(),
            p.getTeam().getId(),
            p.getStatus(),
            p.getProofUrl(),
            p.getComment(),
            p.getCompletedAt()
        );
    }
}
