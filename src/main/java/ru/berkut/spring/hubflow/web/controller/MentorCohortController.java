package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.entity.CohortMembership;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.MentorCohortService;
import ru.berkut.spring.hubflow.web.dto.response.CohortMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
   привязка ментора к когорте.
 */
@RestController
@RequiredArgsConstructor
public class MentorCohortController {

    private final MentorCohortService mentorCohortService;

    // POST /hubflow/api/v1/admin/cohorts/{cohortId}/mentors
    @PostMapping("/admin/cohorts/{cohortId}/mentors")
    public ResponseEntity<CohortMemberResponse> assignMentor(
            @PathVariable UUID cohortId,
            @RequestParam UUID mentorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        CohortMembership m = mentorCohortService.assignToCohort(cohortId, mentorId, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(m));
    }

    // DELETE /hubflow/api/v1/admin/cohorts/{cohortId}/mentors/{mentorId}
    @DeleteMapping("/admin/cohorts/{cohortId}/mentors/{mentorId}")
    public ResponseEntity<Void> removeMentor(
            @PathVariable UUID cohortId,
            @PathVariable UUID mentorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        mentorCohortService.removeFromCohort(cohortId, mentorId, principal);
        return ResponseEntity.noContent().build();
    }

    // GET /hubflow/api/v1/cohorts/{cohortId}/mentors — менторы когорты (публично)
    @GetMapping("/cohorts/{cohortId}/mentors")
    public ResponseEntity<List<CohortMemberResponse>> getMentors(@PathVariable UUID cohortId) {
        return ResponseEntity.ok(mentorCohortService.getMentorsOfCohort(cohortId)
            .stream().map(this::toResponse).toList());
    }

    // GET /hubflow/api/v1/mentors/{mentorId}/cohorts — когорты ментора
    @GetMapping("/mentors/{mentorId}/cohorts")
    public ResponseEntity<List<CohortMemberResponse>> getMentorCohorts(@PathVariable UUID mentorId) {
        return ResponseEntity.ok(mentorCohortService.getCohortsByMentor(mentorId)
            .stream().map(this::toResponse).toList());
    }

    private CohortMemberResponse toResponse(CohortMembership m) {
        return new CohortMemberResponse(
            m.getUser().getId(), m.getUser().getFirstName(),
            m.getUser().getLastName(), m.getUser().getEmail(),
            m.getRole(), m.getJoinedAt());
    }
}
