package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.entity.Cohort;
import ru.berkut.spring.hubflow.entity.CohortMembership;
import ru.berkut.spring.hubflow.enums.CohortStatus;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.CohortService;
import ru.berkut.spring.hubflow.service.TeamService;
import ru.berkut.spring.hubflow.web.dto.request.AddMemberRequest;
import ru.berkut.spring.hubflow.web.dto.request.CreateCohortRequest;
import ru.berkut.spring.hubflow.web.dto.request.UpdateRegistrationRequest;
import ru.berkut.spring.hubflow.web.dto.response.CohortMemberResponse;
import ru.berkut.spring.hubflow.web.dto.response.CohortResponse;
import ru.berkut.spring.hubflow.web.dto.response.TeamResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cohorts")
@RequiredArgsConstructor
public class CohortController {

    private final CohortService cohortService;
    private final TeamService   teamService;
    //спиок всех доступных когорт
    @GetMapping()
    public ResponseEntity<List<CohortResponse>> getPublicCohorts(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CohortResponse> list = cohortService.getPublicCohorts(principal)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }
    //— мои когорты
    @GetMapping("/my-cohorts")
    public ResponseEntity<List<CohortResponse>> getMyCohorts(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CohortResponse> list = cohortService.getUsersAccessibleCohorts(principal)
            .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/registration")
    public ResponseEntity<CohortResponse> updateCohortRegistration(
            @PathVariable UUID id,
            @RequestBody UpdateRegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ){
        Cohort cohort = cohortService.updateRegistration(id,request.registrationOpen(),principal);
        return ResponseEntity.ok(toResponse(cohort));
    }

    // GET /hubflow/api/v1/cohorts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CohortResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(cohortService.getById(id, principal)));
    }

    // POST /hubflow/api/v1/cohorts
    @PostMapping
    public ResponseEntity<CohortResponse> create(
            @Valid @RequestBody CreateCohortRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Cohort cohort = cohortService.create(
            new CohortService.CreateCohortRequest(req.title(), req.description(),
                req.startDate(), req.endDate(), req.totalWeeks(), req.format()), principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(cohort));
    }

    // PATCH /hubflow/api/v1/cohorts/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<CohortResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam CohortStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(cohortService.updateStatus(id, status, principal)));
    }

    // POST /hubflow/api/v1/cohorts/{id}/members
    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        cohortService.addMember(id, req.userId(), req.role(), principal);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /hubflow/api/v1/cohorts/{id}/members
    @GetMapping("/{id}/members")
    public ResponseEntity<List<CohortMemberResponse>> getMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        cohortService.checkMembership(principal.getId(), id);
        List<CohortMemberResponse> members = cohortService.getMembers(id)
            .stream().map(m -> new CohortMemberResponse(
                m.getUser().getId(),
                m.getUser().getFirstName(),
                m.getUser().getLastName(),
                m.getUser().getEmail(),
                m.getRole(),
                m.getJoinedAt()
            )).toList();
        return ResponseEntity.ok(members);
    }

    // GET /hubflow/api/v1/cohorts/{id}/teams — одобренные команды когорты
    @GetMapping("/{id}/teams")
    public ResponseEntity<List<TeamResponse>> getTeams(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(teamService.getApprovedTeams(id, principal)
                .stream().map(t -> new TeamResponse(t.getId(), t.getName(),
                        t.getIdeaDescription(), t.getProblem(), t.getTargetSegment(),
                        t.getSolution(), t.getStage(), t.getRepoUrl(),
                        t.getLandingUrl(), t.getPitchUrl(), t.getCreatedAt()))
                .toList());
    }

    private CohortResponse toResponse(Cohort c) {
        return new CohortResponse(c.getId(), c.getTitle(), c.getDescription(),
            c.getStartDate(), c.getEndDate(), c.getTotalWeeks(),
            c.getFormat(), c.getStatus(), c.getRegistrationOpen(), c.getCreatedAt());
    }
}
