package ru.berkut.spring.hubflow.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.berkut.spring.hubflow.entity.TeamInvite;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.TeamInviteService;
import ru.berkut.spring.hubflow.web.dto.request.InviteUserRequest;
import ru.berkut.spring.hubflow.web.dto.response.TeamInviteResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/team-invites")
@RequiredArgsConstructor
public class TeamInviteController {

    private final TeamInviteService inviteService;

    // GET /hubflow/api/v1/team-invites/my — входящие приглашения текущего пользователя
    @GetMapping("/my")
    public ResponseEntity<List<TeamInviteResponse>> getMyInvites(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(inviteService.getMyInvites(principal)
                .stream().map(this::toInviteResponse).toList());
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> invite(
            @PathVariable UUID id,
            @Valid @RequestBody InviteUserRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        inviteService.invite(id, req.userId(), principal);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<List<TeamInviteResponse>> getTeamInvites(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(inviteService.getTeamInvites(id, principal)
                .stream().map(this::toInviteResponse).toList());
    }

    // принять приглашение
    @PostMapping("/{inviteId}/accept")
    public ResponseEntity<Void> accept(
            @PathVariable UUID inviteId,
            @AuthenticationPrincipal UserPrincipal principal) {

        inviteService.accept(inviteId, principal);
        return ResponseEntity.ok().build();
    }

    // отклонить приглашение
    @PostMapping("/{inviteId}/decline")
    public ResponseEntity<Void> decline(
            @PathVariable UUID inviteId,
            @AuthenticationPrincipal UserPrincipal principal) {

        inviteService.decline(inviteId, principal);
        return ResponseEntity.ok().build();
    }
    private TeamInviteResponse toInviteResponse(TeamInvite i) {
        return new TeamInviteResponse(i.getId(), i.getTeam().getId(), i.getTeam().getName(),
                i.getInvitedUser().getId(), i.getInvitedUser().getFirstName(), i.getInvitedUser().getLastName(),
                i.getStatus(), i.getCreatedAt());
    }
}
