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

import static ru.berkut.spring.hubflow.enums.SystemRole.ADMIN;

@Service
@RequiredArgsConstructor
public class CohortApplicationService {

    private final CohortApplicationRepository applicationRepository;
    private final CohortMembershipRepository  membershipRepository;
    private final TeamMemberRepository        teamMemberRepository;
    private final CohortRepository            cohortRepository;
    private final TeamRepository              teamRepository;
    private final UserRepository              userRepository;
    private final CohortService               cohortService;
    private final TeamService                 teamService;
    private final NotificationService         notificationService;

    // ── Подача заявки командой ──────────────────────────────────────────

    @Transactional
    public CohortApplication apply(UUID cohortId, UUID teamId, UserPrincipal principal) {
        // Подавать заявку может только тимлид
        teamService.requireTeamLead(principal.getId(), teamId);

        Cohort cohort = cohortRepository.findById(cohortId)
            .orElseThrow(() -> NotFoundException.of("Cohort", cohortId));

        if (!cohort.getRegistrationOpen()) {
            throw new BadRequestException("Registration is closed for this cohort");
        }
        if (applicationRepository.findByCohortIdAndTeamId(cohortId, teamId).isPresent()) {
            throw new ConflictException("Application already exists for this team and cohort");
        }

        Team team = teamRepository.getReferenceById(teamId);
        CohortApplication application = applicationRepository.save(CohortApplication.builder()
            .cohort(cohort)
            .team(team)
            .build());

        // Уведомляем админов когорты о новой заявке
        userRepository.findBySystemRole(ADMIN)
            .forEach(admin -> notificationService.send(
                admin.getId(),
                NotificationType.APPLICATION_REVIEWED,
                "Новая заявка от команды",
                "Команда «" + team.getName() + "» подала заявку в когорту «" + cohort.getTitle() + "»",
                null
            ));

        return application;
    }

    // ── Просмотр заявок (ADMIN) ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CohortApplication> getApplications(UUID cohortId, UserPrincipal principal) {
        cohortService.requireAdmin(principal.getId(), cohortId);
        return applicationRepository.findByCohortId(cohortId);
    }

    @Transactional(readOnly = true)
    public List<CohortApplication> getPending(UUID cohortId, UserPrincipal principal) {
        cohortService.requireAdmin(principal.getId(), cohortId);
        return applicationRepository.findByCohortIdAndStatus(cohortId, CohortApplicationStatus.PENDING);
    }

    // ── Одобрение заявки — главная логика ───────────────────────────────

    @Transactional
    public CohortApplication approve(UUID applicationId, UserPrincipal principal) {
        CohortApplication application = getApplicationForReview(applicationId, principal);

        application.setStatus(CohortApplicationStatus.APPROVED);
        application.setReviewedAt(Instant.now());
        application.setReviewedBy(userRepository.getReferenceById(principal.getId()));
        applicationRepository.save(application);

        // ── Автоматически создаём CohortMembership для ВСЕХ участников команды ──
        UUID cohortId = application.getCohort().getId();
        UUID teamId   = application.getTeam().getId();

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        for (TeamMember member : members) {
            UUID userId = member.getUser().getId();

            // Пропускаем, если человек уже состоит в этой когорте
            // (например уже был ментором/админом)
            if (membershipRepository.existsByUserIdAndCohortId(userId, cohortId)) {
                continue;
            }

            membershipRepository.save(CohortMembership.builder()
                .user(member.getUser())
                .cohort(application.getCohort())
                .role(MembershipRole.PARTICIPANT)
                .build());

            notificationService.send(
                userId,
                NotificationType.APPLICATION_REVIEWED,
                "Заявка одобрена!",
                "Команда «" + application.getTeam().getName() + "» принята в когорту «"
                    + application.getCohort().getTitle() + "»",
                null
            );
        }

        return application;
    }

    @Transactional
    public CohortApplication reject(UUID applicationId, UserPrincipal principal) {
        CohortApplication application = getApplicationForReview(applicationId, principal);

        application.setStatus(CohortApplicationStatus.REJECTED);
        application.setReviewedAt(Instant.now());
        application.setReviewedBy(userRepository.getReferenceById(principal.getId()));
        applicationRepository.save(application);

        // Уведомляем тимлида
        teamMemberRepository.findByTeamIdAndRole(application.getTeam().getId(), TeamRole.LEAD)
            .ifPresent(lead -> notificationService.send(
                lead.getUser().getId(),
                NotificationType.APPLICATION_REVIEWED,
                "Заявка отклонена",
                "Заявка команды «" + application.getTeam().getName() + "» в когорту «"
                    + application.getCohort().getTitle() + "» отклонена",
                null
            ));

        return application;
    }

    private CohortApplication getApplicationForReview(UUID applicationId, UserPrincipal principal) {
        CohortApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> NotFoundException.of("CohortApplication", applicationId));

        cohortService.requireAdmin(principal.getId(), application.getCohort().getId());

        if (application.getStatus() != CohortApplicationStatus.PENDING) {
            throw new BadRequestException("Application already reviewed");
        }
        return application;
    }

}
