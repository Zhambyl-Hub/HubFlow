package ru.berkut.spring.hubflow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.Notification;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<


        Notification, UUID> {
    Page<Notification> findByUserIdOrderBySentAtDesc(UUID userId, Pageable pageable);
    long countByUserIdAndIsReadFalse(UUID userId);
}
