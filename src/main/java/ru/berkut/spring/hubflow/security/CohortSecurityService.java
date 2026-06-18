package ru.berkut.spring.hubflow.security;

import ru.berkut.spring.hubflow.enums.MembershipRole;
import ru.berkut.spring.hubflow.enums.SystemRole;
import ru.berkut.spring.hubflow.repository.CohortMembershipRepository;
import ru.berkut.spring.hubflow.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.berkut.spring.hubflow.repository.UserRepository;

import java.util.UUID;

/**
 * Проверка доступа на уровне когорты и команды.
 * Используется в @PreAuthorize("@cohortSecurity.isAdmin(...)") и т.д.
 */
@Service("cohortSecurity")
@RequiredArgsConstructor
public class CohortSecurityService {

    private final UserRepository userRepository;
    private final CohortMembershipRepository cohortMembershipRepository;
    private final TeamMemberRepository teamMemberRepository;

    /** Является ли пользователь ADMIN-ом когорты */
    public boolean isAdmin(UUID userId) {
        return userRepository.findById(userId)
                .map(u -> u.getSystemRole() == SystemRole.ADMIN)
                .orElse(false);
    }

    /** Является ли пользователь MENTOR-ом когорты */
    public boolean isMentor(UUID userId, UUID cohortId) {
        return hasRole(userId, cohortId, MembershipRole.MENTOR);
    }

    /** Является ли пользователь участником когорты (любая роль) */
    public boolean isMember(UUID userId, UUID cohortId) {
        return cohortMembershipRepository.existsByUserIdAndCohortId(userId, cohortId);
    }

    /** Является ли пользователь ADMIN или MENTOR когорты */
    public boolean isAdminOrMentor(UUID userId, UUID cohortId) {
        return isAdmin(userId) || isMentor(userId, cohortId);
    }

    /** Состоит ли пользователь в команде */
    public boolean isTeamMember(UUID userId, UUID teamId) {
        return teamMemberRepository.findByTeamIdAndUserId(teamId, userId).isPresent();
    }

    private boolean hasRole(UUID userId, UUID cohortId, MembershipRole role) {
        return cohortMembershipRepository
            .findByUserIdAndCohortId(userId, cohortId)
            .map(m -> m.getRole() == role)
            .orElse(false);
    }
    public boolean hasAccess(UUID userId, UUID cohortId) {
        return isAdmin(userId) || isMentor(userId, cohortId)
                || cohortMembershipRepository.existsByUserIdAndCohortId(userId, cohortId);
    }
}
