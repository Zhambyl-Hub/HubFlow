package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.entity.DemoCriteria;
import ru.berkut.spring.hubflow.entity.DemoDay;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.DemoDayService;
import ru.berkut.spring.hubflow.web.dto.request.*;
import ru.berkut.spring.hubflow.web.dto.response.DemoCriteriaResponse;
import ru.berkut.spring.hubflow.web.dto.response.DemoDayResponse;
import ru.berkut.spring.hubflow.web.dto.response.VoteResultResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cohorts/{cohortId}/demo-day")
@RequiredArgsConstructor
public class DemoDayController {

    private final DemoDayService demoDayService;

    // POST /hubflow/api/v1/cohorts/{cohortId}/demo-day
    @PostMapping
    public ResponseEntity<DemoDayResponse> create(
            @PathVariable UUID cohortId,
            @Valid @RequestBody CreateDemoDayRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        DemoDay day = demoDayService.create(
                new DemoDayService.CreateDemoDayRequest(cohortId, req.eventDate(), req.description()),
                principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(day));
    }
    @GetMapping
    public ResponseEntity<DemoDayResponse> get(
            @PathVariable UUID cohortId) {
        return demoDayService.getByCohort(cohortId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PATCH /hubflow/api/v1/cohorts/{cohortId}/demo-day/{demoDayId}/open-voting
    @PatchMapping("/{demoDayId}/open-voting")
    public ResponseEntity<DemoDayResponse> openVoting(
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(demoDayService.openVoting(demoDayId, principal)));
    }

    // PATCH /hubflow/api/v1/cohorts/{cohortId}/demo-day/{demoDayId}/close-voting
    @PatchMapping("/{demoDayId}/close-voting")
    public ResponseEntity<DemoDayResponse> closeVoting(
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(demoDayService.closeVoting(demoDayId, principal)));
    }

    @PatchMapping("/{demoDayId}/show_results")
    public ResponseEntity<DemoDayResponse> show_results(
            @PathVariable UUID demoDayId,
            @RequestBody ShowResultsRequest req,
            @AuthenticationPrincipal UserPrincipal principal
    ){
        return ResponseEntity.ok(toResponse(demoDayService.showResults(demoDayId,req.show_results(),principal)));
    }


    // ─────────── Критерии оценки ───────────

    // POST /hubflow/api/v1/cohorts/{cohortId}/demo-day/{demoDayId}/criteria
    @PostMapping("/{demoDayId}/criteria")
    public ResponseEntity<DemoCriteriaResponse> addCriteria(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @Valid @RequestBody AddCriteriaRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        DemoCriteria criteria = demoDayService.addCriteria(demoDayId, req, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCriteriaResponse(criteria));
    }

    // GET /hubflow/api/v1/cohorts/{cohortId}/demo-day/{demoDayId}/criteria
    @GetMapping("/{demoDayId}/criteria")
    public ResponseEntity<List<DemoCriteriaResponse>> listCriteria(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DemoCriteriaResponse> list = demoDayService.listCriteria(demoDayId, principal)
                .stream().map(this::toCriteriaResponse).toList();
        return ResponseEntity.ok(list);
    }

    // DELETE /hubflow/api/v1/cohorts/{cohortId}/demo-day/{demoDayId}/criteria/{criterionId}
    @DeleteMapping("/{demoDayId}/criteria/{criterionId}")
    public ResponseEntity<Void> deleteCriteria(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @PathVariable UUID criterionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        demoDayService.deleteCriteria(demoDayId, criterionId, principal);
        return ResponseEntity.noContent().build();
    }

    private DemoCriteriaResponse toCriteriaResponse(DemoCriteria c) {
        return new DemoCriteriaResponse(c.getId(), c.getTitle(), c.getDescription(),
                c.getMaxScore(), c.getOrderIndex());
    }

    private DemoDayResponse toResponse(DemoDay d) {
        return new DemoDayResponse(d.getId(), d.getCohort().getId(), d.getEventDate(),
                d.getDescription(), d.getVotingStatus(), d.getShowResultsPublicly(),
                d.getVotingOpensAt(), d.getVotingClosesAt(), List.of());
    }
}
