package ru.berkut.spring.hubflow.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berkut.spring.hubflow.entity.MentorProfile;
import ru.berkut.spring.hubflow.entity.SystemRoleHistory;
import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.enums.SystemRole;
import ru.berkut.spring.hubflow.exception.AccessDeniedException;
import ru.berkut.spring.hubflow.exception.BadRequestException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.MentorProfileRepository;
import ru.berkut.spring.hubflow.repository.SystemRoleHistoryRepository;
import ru.berkut.spring.hubflow.repository.UserRepository;
import ru.berkut.spring.hubflow.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final SystemRoleHistoryRepository roleHistoryRepository;

    // ── Список всех пользователей ───────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<User> getUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional(readOnly = true)
    public List<User> getMentors(UserPrincipal principal) {
        requireAdmin(principal);
        return userRepository.findBySystemRole(SystemRole.MENTOR);
    }

    // ── Этап 1: смена системной роли ───────────────────────────────────

    @Transactional
    public User changeSystemRole(UUID targetUserId, SystemRole newRole, UserPrincipal principal) {
        requireAdmin(principal);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> NotFoundException.of("User", targetUserId));

        SystemRole oldRole = target.getSystemRole();
        if (oldRole == newRole) {
            throw new BadRequestException("User already has role: " + newRole);
        }
        // Нельзя лишить себя роли ADMIN
        if (targetUserId.equals(principal.getId()) && oldRole == SystemRole.ADMIN) {
            throw new BadRequestException("Cannot remove your own ADMIN role");
        }

        target.setSystemRole(newRole);
        userRepository.save(target);

        // Пишем в историю
        User changedBy = userRepository.getReferenceById(principal.getId());
        roleHistoryRepository.save(SystemRoleHistory.builder()
                .user(target)
                .oldRole(oldRole)
                .newRole(newRole)
                .changedBy(changedBy)
                .build());

        // При назначении MENTOR — автоматически создаём профиль
        if (newRole == SystemRole.MENTOR && !mentorProfileRepository.existsByUserId(targetUserId)) {
            mentorProfileRepository.save(MentorProfile.builder()
                    .user(target)
                    .build());
        }

        return target;
    }

    @Transactional(readOnly = true)
    public List<SystemRoleHistory> getRoleHistory(UUID userId, UserPrincipal principal) {
        requireAdmin(principal);
        return roleHistoryRepository.findByUserIdOrderByChangedAtDesc(userId);
    }

    // ── Вспомогательное ────────────────────────────────────────────────

    public void requireAdmin(UserPrincipal principal) {
        if (!principal.isAdmin()) {
            throw new AccessDeniedException("ADMIN role required");
        }
    }
}
