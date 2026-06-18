package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.*;
import ru.berkut.spring.hubflow.enums.*;
import ru.berkut.spring.hubflow.repository.*;
import ru.berkut.spring.hubflow.exception.*;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamInviteService {

    private final TeamInviteRepository inviteRepository;
    private final TeamRepository       teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository       userRepository;
    private final TeamService          teamService;
    private final NotificationService  notificationService;

    // ── Отправка приглашения (только TEAM_LEAD) ─────────────────────────

    @Transactional
    public TeamInvite invite(UUID teamId, UUID invitedUserId, UserPrincipal principal) {
        teamService.requireTeamLead(principal.getId(), teamId);

        if (teamMemberRepository.findByTeamIdAndUserId(teamId, invitedUserId).isPresent()) {
            throw new ConflictException("User is already a team member");
        }
        if (inviteRepository.existsByTeamIdAndInvitedUserIdAndStatus(
                teamId, invitedUserId, InviteStatus.PENDING)) {
            throw new ConflictException("Invite already sent");
        }

        Team team       = teamRepository.getReferenceById(teamId);
        User invitedUser = userRepository.findById(invitedUserId)
            .orElseThrow(() -> NotFoundException.of("User", invitedUserId));
        User invitedBy  = userRepository.getReferenceById(principal.getId());

        TeamInvite invite = inviteRepository.save(TeamInvite.builder()
            .team(team)
            .invitedUser(invitedUser)
            .invitedBy(invitedBy)
            .build());

        notificationService.send(
            invitedUserId,
            NotificationType.TEAM_INVITE,
            "Приглашение в команду",
            "Вас пригласили в команду «" + team.getName() + "»",
            null
        );

        return invite;
    }

    // ── Просмотр своих приглашений ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TeamInvite> getMyInvites(UserPrincipal principal) {
        return inviteRepository.findByInvitedUserIdAndStatus(principal.getId(), InviteStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<TeamInvite> getTeamInvites(UUID teamId, UserPrincipal principal) {
        teamService.requireTeamLead(principal.getId(), teamId);
        return inviteRepository.findByTeamId(teamId);
    }

    // ── Принять / отклонить ─────────────────────────────────────────────

    @Transactional
    public void accept(UUID inviteId, UserPrincipal principal) {
        TeamInvite invite = getOwnPendingInvite(inviteId, principal);

        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setRespondedAt(Instant.now());
        inviteRepository.save(invite);

        // Добавляем пользователя в команду как MEMBER
        User acceptor  = userRepository.getReferenceById(principal.getId());
        teamMemberRepository.save(TeamMember.builder()
            .team(invite.getTeam())
            .user(acceptor )
            .role(TeamRole.MEMBER)
            .build());

        //ищем Invited_by
        User invitedBy = invite.getInvitedBy();
        notificationService.send(
                invitedBy.getId(),
                NotificationType.TEAM_INVITE_ACCEPTED,
                "Приглашение в команду принята",
                "Участник который принял приглашение «" + acceptor.getFirstName()+" "+acceptor.getLastName() + "»",
                null
        );
    }

    @Transactional
    public void decline(UUID inviteId, UserPrincipal principal) {
        TeamInvite invite = getOwnPendingInvite(inviteId, principal);
        invite.setStatus(InviteStatus.DECLINED);
        invite.setRespondedAt(Instant.now());
        inviteRepository.save(invite);
        User acceptor  = userRepository.getReferenceById(principal.getId());
        //ищем Invited_by
        User invitedBy = invite.getInvitedBy();
        notificationService.send(
                invitedBy.getId(),
                NotificationType.TEAM_INVITE_DECLINED,
                "Приглашение в команду отклонено",
                "Участник который отклонил приглашение «" + acceptor.getFirstName()+" "+acceptor.getLastName() + "»",
                null
        );
    }

    private TeamInvite getOwnPendingInvite(UUID inviteId, UserPrincipal principal) {
        TeamInvite invite = inviteRepository.findById(inviteId)
            .orElseThrow(() -> NotFoundException.of("TeamInvite", inviteId));

        if (!invite.getInvitedUser().getId().equals(principal.getId())) {
            throw new AccessDeniedException("Not your invite");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invite already responded");
        }
        return invite;
    }
}
