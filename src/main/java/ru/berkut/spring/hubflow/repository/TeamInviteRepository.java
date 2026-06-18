package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.TeamInvite;
import ru.berkut.spring.hubflow.enums.InviteStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamInviteRepository extends JpaRepository<TeamInvite, UUID> {
    Optional<TeamInvite> findByTeamIdAndInvitedUserId(UUID teamId, UUID invitedUserId);
    List<TeamInvite> findByInvitedUserId(UUID invitedUserId);
    boolean existsByTeamIdAndInvitedUserIdAndStatus(UUID teamId, UUID userId, InviteStatus status);
    List<TeamInvite> findByInvitedUserIdAndStatus(UUID id, InviteStatus inviteStatus);
    List<TeamInvite> findByTeamId(UUID teamId);
}
