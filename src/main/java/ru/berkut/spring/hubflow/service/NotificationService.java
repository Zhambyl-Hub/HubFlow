package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.Notification;
import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.enums.NotificationType;
import ru.berkut.spring.hubflow.exception.AccessDeniedException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.CohortMembershipRepository;
import ru.berkut.spring.hubflow.repository.NotificationRepository;
import ru.berkut.spring.hubflow.repository.UserRepository;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository     notificationRepository;
    private final CohortMembershipRepository membershipRepository;
    private final UserRepository             userRepository;

    @Transactional
    public void send(UUID userId, NotificationType type,
                     String title, String body, Map<String, Object> metadata) {
        User user = userRepository.getReferenceById(userId);
        notificationRepository.save(Notification.builder()
            .user(user).type(type).title(title).body(body).metadata(metadata)
            .build());
    }

    // Рассылка всем участникам когорты
    @Transactional
    public void notifyCohort(UUID cohortId, NotificationType type,
                             String title, String body, Map<String, Object> metadata) {
        membershipRepository.findByCohortId(cohortId).forEach(m ->
            send(m.getUser().getId(), type, title, body, metadata));
    }

    @Transactional(readOnly = true)
    public Page<Notification> getForUser(UserPrincipal principal, int page, int size) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(
            principal.getId(), PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal principal) {
        return notificationRepository.countByUserIdAndIsReadFalse(principal.getId());
    }

    @Transactional
    public void markRead(UUID notificationId, UserPrincipal principal) {
        Notification n = notificationRepository.findById(notificationId)
            .orElseThrow(() -> NotFoundException.of("Notification", notificationId));
        if (!n.getUser().getId().equals(principal.getId())) {
            throw new AccessDeniedException();
        }
        n.setIsRead(true);
        n.setReadAt(Instant.now());
        notificationRepository.save(n);
    }
}
