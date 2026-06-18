package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.entity.CohortApplication;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.CohortApplicationService;
import ru.berkut.spring.hubflow.web.dto.request.ApplyCohortRequest;
import ru.berkut.spring.hubflow.web.dto.response.CohortApplicationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CohortApplicationController {

    private final CohortApplicationService applicationService;

    // POST /hubflow/api/v1/cohorts/{cohortId}/applications — команда подаёт заявку (LEAD)
    @PostMapping("/cohorts/{cohortId}/applications")
    public ResponseEntity<CohortApplicationResponse> apply(
            @PathVariable UUID cohortId,
            @Valid @RequestBody ApplyCohortRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        CohortApplication app = applicationService.apply(cohortId, req.teamId(), principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(app));
    }

    // GET /hubflow/api/v1/cohorts/{cohortId}/applications — все заявки (ADMIN)
    @GetMapping("/cohorts/{cohortId}/applications")
    public ResponseEntity<List<CohortApplicationResponse>> getApplications(
            @PathVariable UUID cohortId,
            @RequestParam(required = false) Boolean pendingOnly,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CohortApplication> apps = Boolean.TRUE.equals(pendingOnly)
            ? applicationService.getPending(cohortId, principal)
            : applicationService.getApplications(cohortId, principal);
        return ResponseEntity.ok(apps.stream().map(this::toResponse).toList());
    }

    // PATCH /hubflow/api/v1/applications/{id}/approve — одобрить (ADMIN)
    @PatchMapping("/applications/{id}/approve")
    public ResponseEntity<CohortApplicationResponse> approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(applicationService.approve(id, principal)));
    }

    // PATCH /hubflow/api/v1/applications/{id}/reject — отклонить (ADMIN)
    @PatchMapping("/applications/{id}/reject")
    public ResponseEntity<CohortApplicationResponse> reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(applicationService.reject(id, principal)));
    }

    private CohortApplicationResponse toResponse(CohortApplication a) {
        return new CohortApplicationResponse(
            a.getId(), a.getCohort().getId(), a.getCohort().getTitle(),
            a.getTeam().getId(), a.getTeam().getName(),
            a.getStatus(), a.getAppliedAt(), a.getReviewedAt()
        );
    }
}
