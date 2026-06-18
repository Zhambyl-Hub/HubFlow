package ru.berkut.spring.hubflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berkut.spring.hubflow.entity.Team;
import ru.berkut.spring.hubflow.entity.TeamMember;
import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.enums.TeamRole;
import ru.berkut.spring.hubflow.exception.AccessDeniedException;
import ru.berkut.spring.hubflow.exception.BadRequestException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.TeamMemberRepository;
import ru.berkut.spring.hubflow.repository.TeamRepository;
import ru.berkut.spring.hubflow.repository.UserRepository;
import ru.berkut.spring.hubflow.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository       teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository       userRepository;

    public record CreateTeamRequest(String name, String ideaDescription,
                                    String problem, String targetSegment, String solution) {}

    // ── Создание команды ────────────────────────────────────────────────

    @Transactional
    public Team create(CreateTeamRequest req, UserPrincipal principal) {
        User creator = userRepository.getReferenceById(principal.getId());

        Team team = Team.builder()
                .name(req.name())
                .ideaDescription(req.ideaDescription())
                .problem(req.problem())
                .targetSegment(req.targetSegment())
                .solution(req.solution())
                .createdBy(creator)
                .build();
        teamRepository.save(team);

        // Создатель автоматически становится TEAM_LEAD (роль LEAD)
        teamMemberRepository.save(TeamMember.builder()
                .team(team).user(creator).role(TeamRole.LEAD).build());

        return team;
    }

    @Transactional(readOnly = true)
    public List<Team> getMyTeams(UserPrincipal principal) {
        return teamRepository.findByMemberUserId(principal.getId());
    }

    @Transactional(readOnly = true)
    public Team getById(UUID teamId, UserPrincipal principal) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> NotFoundException.of("Team", teamId));
        requireTeamMember(principal.getId(), teamId);
        return team;
    }

    // ── Состав команды ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TeamMember> getMembers(UUID teamId, UserPrincipal principal) {
        requireTeamMember(principal.getId(), teamId);
        return teamMemberRepository.findByTeamId(teamId);
    }

    @Transactional
    public void removeMember(UUID teamId, UUID userId, UserPrincipal principal) {
        requireTeamLead(principal.getId(), teamId);

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        if (member.getRole() == TeamRole.LEAD) {
            throw new BadRequestException("Cannot remove team lead");
        }
        teamMemberRepository.delete(member);
    }

    // ── Вспомогательные методы проверки прав ───────────────────────────

    public Team requireTeamMember(UUID userId, UUID teamId) {
        teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new AccessDeniedException("Not a team member"));
        return teamRepository.findById(teamId)
                .orElseThrow(() -> NotFoundException.of("Team", teamId));
    }

    public void requireTeamLead(UUID userId, UUID teamId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new AccessDeniedException("Not a team member"));
        if (member.getRole() != TeamRole.LEAD) {
            throw new AccessDeniedException("Team lead role required");
        }
    }
}
