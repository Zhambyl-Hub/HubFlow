package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.*;
import ru.berkut.spring.hubflow.enums.*;
import ru.berkut.spring.hubflow.repository.*;
import ru.berkut.spring.hubflow.exception.*;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Только ADMIN может привязывать/отвязывать.
 * Ментор может быть привязан к N когортам одновременно.
 */
@Service
@RequiredArgsConstructor
public class MentorCohortService {

    private final CohortMembershipRepository membershipRepository;
    private final UserRepository             userRepository;
    private final CohortRepository           cohortRepository;
    private final AdminService               adminService;
    private final NotificationService        notificationService;

    // ── Привязать ментора к когорте ─────────────────────────────────────

    @Transactional
    public CohortMembership assignToCohort(UUID cohortId, UUID mentorId, UserPrincipal principal) {
        adminService.requireAdmin(principal);

        User mentor = userRepository.findById(mentorId)
            .orElseThrow(() -> NotFoundException.of("User", mentorId));

        // Можно привязать только пользователя с system_role = MENTOR
        if (!mentor.isMentor()) {
            throw new BadRequestException(
                "User is not a mentor. Assign MENTOR role first via PATCH /admin/users/{id}/role");
        }

        Cohort cohort = cohortRepository.findById(cohortId)
            .orElseThrow(() -> NotFoundException.of("Cohort", cohortId));

        // Проверяем что ещё не привязан
        if (membershipRepository.existsByUserIdAndCohortId(mentorId, cohortId)) {
            throw new ConflictException("Mentor is already assigned to this cohort");
        }

        CohortMembership membership = membershipRepository.save(CohortMembership.builder()
            .user(mentor)
            .cohort(cohort)
            .role(MembershipRole.MENTOR)
            .build());

        notificationService.send(
            mentorId,
            NotificationType.APPLICATION_REVIEWED,
            "Вы назначены ментором",
            "Вы добавлены как ментор в когорту «" + cohort.getTitle() + "»",
            null
        );

        return membership;
    }

    // ── Отвязать ментора от когорты ─────────────────────────────────────

    @Transactional
    public void removeFromCohort(UUID cohortId, UUID mentorId, UserPrincipal principal) {
        adminService.requireAdmin(principal);

        CohortMembership membership = membershipRepository
            .findByUserIdAndCohortId(mentorId, cohortId)
            .orElseThrow(() -> new NotFoundException("Mentor is not assigned to this cohort"));

        if (membership.getRole() != MembershipRole.MENTOR) {
            throw new BadRequestException("User is not a mentor in this cohort");
        }

        membershipRepository.delete(membership);
    }

    // ── Менторы когорты ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CohortMembership> getMentorsOfCohort(UUID cohortId) {
        return membershipRepository.findByCohortIdAndRole(cohortId, MembershipRole.MENTOR);
    }

    // ── Когорты ментора ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CohortMembership> getCohortsByMentor(UUID mentorId) {
        return membershipRepository.findByUserIdAndRole(mentorId, MembershipRole.MENTOR);
    }
}
