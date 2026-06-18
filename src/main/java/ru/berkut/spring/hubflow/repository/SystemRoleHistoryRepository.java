package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.SystemRoleHistory;

import java.util.List;
import java.util.UUID;

public interface SystemRoleHistoryRepository extends JpaRepository<SystemRoleHistory, UUID> {
    List<SystemRoleHistory> findByUserIdOrderByChangedAtDesc(UUID userId);
}
