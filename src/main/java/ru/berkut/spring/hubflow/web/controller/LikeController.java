package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.CohortService;
import ru.berkut.spring.hubflow.service.LikeService;
import ru.berkut.spring.hubflow.web.dto.response.LikeCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cohorts/{cohortId}/demo-day/{demoDayId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService   likeService;
    private final CohortService cohortService;

    // POST /cohorts/{cohortId}/demo-day/{demoDayId}/likes/{teamId}
    @PostMapping("/{teamId}")
    public ResponseEntity<Void> like(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserPrincipal principal) {
        likeService.like(demoDayId, teamId, principal);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // DELETE /cohorts/{cohortId}/demo-day/{demoDayId}/likes/{teamId}
    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> unlike(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserPrincipal principal) {
        likeService.unlike(demoDayId, teamId, principal);
        return ResponseEntity.noContent().build();
    }

    // GET /cohorts/{cohortId}/demo-day/{demoDayId}/likes
    @GetMapping
    public ResponseEntity<List<LikeCountResponse>> getLikeCounts(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(cohortId, principal);
        return ResponseEntity.ok(likeService.getLikeCounts(demoDayId, principal));
    }

    private void requireMember(UUID cohortId, UserPrincipal principal) {
        if (!principal.isAdmin()) {
            cohortService.checkMembership(principal.getId(), cohortId);
        }
    }
}
