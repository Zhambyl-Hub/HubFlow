package ru.berkut.spring.hubflow.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import ru.berkut.spring.hubflow.entity.Cohort;
import ru.berkut.spring.hubflow.entity.CohortMembership;
import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.enums.CohortFormat;
import ru.berkut.spring.hubflow.enums.CohortStatus;
import ru.berkut.spring.hubflow.enums.MembershipRole;
import ru.berkut.spring.hubflow.enums.SystemRole;
import ru.berkut.spring.hubflow.exception.AccessDeniedException;
import ru.berkut.spring.hubflow.exception.ConflictException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.CohortMembershipRepository;
import ru.berkut.spring.hubflow.repository.CohortRepository;
import ru.berkut.spring.hubflow.repository.UserRepository;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berkut.spring.hubflow.web.dto.response.CohortResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CohortService {

    private final CohortRepository cohortRepository;
    private final CohortMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    @Transactional()
    public Cohort updateRegistration(UUID id, @NotNull boolean b, UserPrincipal principal) {
        requireAdmin(principal.getId(),id);

        Cohort cohort = cohortRepository.findById(id).orElseThrow(() -> new NotFoundException("Cohort not found"));
        if (cohort.getStatus() == CohortStatus.COMPLETED
                || cohort.getStatus() == CohortStatus.ARCHIVED) {
            throw new ConflictException(
                    "Cannot open registration for completed cohort");
        }
        cohort.setRegistrationOpen(b);
        return cohort;
    }

    public boolean isAdmin(UUID id, UUID id1) {
        return userRepository.findById(id).map(u->u.getSystemRole()==SystemRole.ADMIN).orElse(false);
    }


    public record CreateCohortRequest(String title, String description,
                                      java.time.LocalDate startDate,
                                      java.time.LocalDate endDate,
                                      int totalWeeks, CohortFormat format) {
    }
    @Transactional(readOnly = true)
    public List<Cohort> getPublicCohorts(UserPrincipal principal) {
        return cohortRepository.findByRegistrationOpenTrue();
    }
    @Transactional(readOnly = true)
    public List<Cohort> getUsersAccessibleCohorts(UserPrincipal principal) {
        return membershipRepository.findByUserId(principal.getId())
                .stream()
                .map(CohortMembership::getCohort)
                .toList();
    }

    @Transactional(readOnly = true)
    public Cohort getById(UUID cohortId, UserPrincipal principal) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> NotFoundException.of("Cohort", cohortId));
        checkMembership(principal.getId(), cohortId);
        return cohort;
    }

    @Transactional
    public Cohort create(CreateCohortRequest req, UserPrincipal principal) {
        if (!principal.isAdmin()) {
            throw new AccessDeniedException("Only ADMIN can create cohorts");
        }
        User creator = userRepository.getReferenceById(principal.getId());
        Cohort cohort = Cohort.builder()
                .title(req.title())
                .description(req.description())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .totalWeeks(req.totalWeeks())
                .format(req.format())
                .createdBy(creator)
                .build();
        cohortRepository.save(cohort);
        return cohort;
    }

    @Transactional
    public Cohort updateStatus(UUID cohortId, CohortStatus newStatus, UserPrincipal principal) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> NotFoundException.of("Cohort", cohortId));
        requireAdmin(principal.getId(), cohortId);
        cohort.setStatus(newStatus);
        return cohortRepository.save(cohort);
    }

    @Transactional
    public void addMember(UUID cohortId, UUID userId, MembershipRole role, UserPrincipal principal) {
        requireAdmin(principal.getId(), cohortId);

        if (membershipRepository.existsByUserIdAndCohortId(userId, cohortId)) {
            throw new ConflictException("User already member of cohort");
        }
        User user = userRepository.getReferenceById(userId);
        Cohort cohort = cohortRepository.getReferenceById(cohortId);
        membershipRepository.save(CohortMembership.builder()
                .user(user).cohort(cohort).role(role).build());
    }

    // ── Вспомогательные методы проверки прав ──────────────────────────

    public void checkMembership(UUID userId, UUID cohortId) {
        if (!membershipRepository.existsByUserIdAndCohortId(userId, cohortId)) {
            throw new AccessDeniedException("Not a member of this cohort");
        }
    }

    public void requireAdmin(UUID userId, UUID cohortId) {
        User user =  userRepository.findById(userId).orElseThrow(AccessDeniedException::new);
        if (user.getSystemRole()!= SystemRole.ADMIN) {
            throw new AccessDeniedException("Admin role required");
        }
    }

    public void requireAdminOrMentor(UUID userId, UUID cohortId) {
        User user = userRepository.findById(userId).orElseThrow(AccessDeniedException::new);
        // ADMIN глобальный — пропускаем сразу
        if (user.getSystemRole() == SystemRole.ADMIN) return;
        // MENTOR должен быть привязан к этой когорте
        CohortMembership m = membershipRepository.findByUserIdAndCohortId(userId, cohortId)
                .orElseThrow(() -> new AccessDeniedException("Not a member of this cohort"));
        if (m.getRole() != MembershipRole.MENTOR) {
            throw new AccessDeniedException("Admin or Mentor role required");
        }
    }
    public void requireMentor(UUID userId, UUID cohortId) {
        CohortMembership m = membershipRepository.findByUserIdAndCohortId(userId, cohortId)
                .orElseThrow(AccessDeniedException::new);
        if (m.getRole() == MembershipRole.PARTICIPANT) {
            throw new AccessDeniedException("Only Mentor role required");
        }
    }

    @Transactional(readOnly = true)
    public List<CohortMembership> getMembers(UUID cohortId) {
        return membershipRepository.findByCohortId(cohortId);
    }
}

