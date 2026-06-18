package ru.berkut.spring.hubflow.web.controller;

import org.springframework.web.bind.annotation.*;
import ru.berkut.spring.hubflow.entity.Team;
import ru.berkut.spring.hubflow.entity.TeamInvite;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.TeamInviteService;
import ru.berkut.spring.hubflow.service.TeamService;
import ru.berkut.spring.hubflow.web.dto.request.CreateTeamRequest;
import ru.berkut.spring.hubflow.web.dto.request.InviteUserRequest;
import ru.berkut.spring.hubflow.web.dto.response.TeamInviteResponse;
import ru.berkut.spring.hubflow.web.dto.response.TeamMemberResponse;
import ru.berkut.spring.hubflow.web.dto.response.TeamResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService       teamService;
    private final TeamInviteService inviteService;

    // GET /hubflow/api/v1/teams — мои команды
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getMyTeams(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(teamService.getMyTeams(principal)
                .stream().map(this::toResponse).toList());
    }

    // GET /hubflow/api/v1/teams/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(teamService.getById(id, principal)));
    }

    // POST /hubflow/api/v1/teams — создать команду (пользователь становится LEAD)
    @PostMapping
    public ResponseEntity<TeamResponse> create(
            @Valid @RequestBody CreateTeamRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Team team = teamService.create(new TeamService.CreateTeamRequest(
                req.name(), req.ideaDescription(), req.problem(),
                req.targetSegment(), req.solution()), principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(team));
    }

    // GET /hubflow/api/v1/teams/{id}/members
    @GetMapping("/{id}/members")
    public ResponseEntity<List<TeamMemberResponse>> getMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(teamService.getMembers(id, principal).stream()
                .map(m -> new TeamMemberResponse(
                        m.getUser().getId(), m.getUser().getFirstName(),
                        m.getUser().getLastName(), m.getUser().getEmail(), m.getRole()))
                .toList());
    }

    // DELETE /hubflow/api/v1/teams/{id}/members/{userId}
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        teamService.removeMember(id, userId, principal);
        return ResponseEntity.noContent().build();
    }


    private TeamResponse toResponse(Team t) {
        return new TeamResponse(t.getId(), t.getName(),
                t.getIdeaDescription(), t.getProblem(), t.getTargetSegment(), t.getSolution(),
                t.getStage(), t.getRepoUrl(), t.getLandingUrl(), t.getPitchUrl(), t.getCreatedAt());
    }

    private TeamInviteResponse toInviteResponse(TeamInvite i) {
        return new TeamInviteResponse(i.getId(), i.getTeam().getId(), i.getTeam().getName(),
                i.getInvitedUser().getId(), i.getInvitedUser().getFirstName(), i.getInvitedUser().getLastName(),
                i.getStatus(), i.getCreatedAt());
    }
}
