package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.service.AuthService.AuthResponse;
import ru.berkut.spring.hubflow.service.JuryService;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.web.dto.request.ActivateJuryInviteRequest;
import ru.berkut.spring.hubflow.web.dto.request.CreateJuryInviteRequest;
import ru.berkut.spring.hubflow.web.dto.request.JuryVoteRequest;
import ru.berkut.spring.hubflow.web.dto.response.JuryInviteResponse;
import ru.berkut.spring.hubflow.web.dto.response.JuryPublicPageResponse;
import ru.berkut.spring.hubflow.web.dto.response.JuryVoteResponse;
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
public class JuryController {

    private final JuryService juryService;

    // ─────────── Управление приглашениями (ADMIN) ───────────

    /**
     * POST /cohorts/{cohortId}/demo-day/{demoDayId}/jury-invites
     * Создать приглашение для члена жюри.
     */
    @PostMapping("/cohorts/{cohortId}/demo-day/{demoDayId}/jury-invites")
    public ResponseEntity<JuryInviteResponse> createInvite(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @Valid @RequestBody CreateJuryInviteRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        JuryInviteResponse response = juryService.createInvite(demoDayId, req, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /cohorts/{cohortId}/demo-day/{demoDayId}/jury-invites
     * Список всех приглашений (статус, кто активировал).
     */
    @GetMapping("/cohorts/{cohortId}/demo-day/{demoDayId}/jury-invites")
    public ResponseEntity<List<JuryInviteResponse>> listInvites(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(juryService.listInvites(demoDayId, principal));
    }

    /**
     * DELETE /cohorts/{cohortId}/demo-day/{demoDayId}/jury-invites/{inviteId}
     * Отозвать приглашение (только PENDING).
     */
    @DeleteMapping("/cohorts/{cohortId}/demo-day/{demoDayId}/jury-invites/{inviteId}")
    public ResponseEntity<Void> revokeInvite(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @PathVariable UUID inviteId,
            @AuthenticationPrincipal UserPrincipal principal) {
        juryService.revokeInvite(demoDayId, inviteId, principal);
        return ResponseEntity.noContent().build();
    }

    // ─────────── Активация приглашения (PUBLIC) ───────────

    /**
     * POST /jury/activate?token=XXX
     * Жюри открывает ссылку, вводит имя + пароль.
     * Возвращает JWT — сразу авторизован.
     */
    @PostMapping("/jury/activate")
    public ResponseEntity<AuthResponse> activateInvite(
            @RequestParam String token,
            @Valid @RequestBody ActivateJuryInviteRequest req) {
        AuthResponse response = juryService.activateInvite(token, req);
        return ResponseEntity.ok(response);
    }

    // ─────────── Голосование (JURY) ───────────

    /**
     * POST /jury/demo-day/{demoDayId}/votes
     * Жюри выставляет оценку команде по критерию.
     */
    @PostMapping("/jury/demo-day/{demoDayId}/votes")
    public ResponseEntity<JuryVoteResponse> vote(
            @PathVariable UUID demoDayId,
            @Valid @RequestBody JuryVoteRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        JuryVoteResponse response = juryService.vote(demoDayId, req, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /jury/demo-day/{demoDayId}/my-votes
     * Посмотреть свои оценки.
     */
    @GetMapping("/jury/demo-day/{demoDayId}/my-votes")
    public ResponseEntity<List<JuryVoteResponse>> getMyVotes(
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(juryService.getMyVotes(demoDayId, principal));
    }
    /**
     * GET /jury/demo-day/{demoDayId}/public
     * Страница Demo Day для члена жюри: критерии оценки и команды-участницы.
     */
    @GetMapping("/jury/demo-day/{demoDayId}/public")
    public ResponseEntity<JuryPublicPageResponse> getPublicPage(
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(juryService.getPublicPage(demoDayId, principal));
    }
}