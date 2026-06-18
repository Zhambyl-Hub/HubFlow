package ru.berkut.spring.hubflow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.TeamMember;
import ru.berkut.spring.hubflow.enums.TeamRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamId(UUID teamId);
    List<TeamMember> findByUserId(UUID userId);
    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);
    Optional<TeamMember> findByTeamIdAndRole(UUID teamId, TeamRole role);
}
