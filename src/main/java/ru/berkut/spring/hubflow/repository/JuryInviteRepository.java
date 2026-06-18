package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.JuryInvite;
import ru.berkut.spring.hubflow.enums.JuryInviteStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JuryInviteRepository extends JpaRepository<JuryInvite, UUID> {
    Optional<JuryInvite> findByInviteToken(String token);
    List<JuryInvite> findByDemoDayIdOrderByCreatedAtDesc(UUID demoDayId);
    boolean existsByDemoDayIdAndEmail(UUID demoDayId, String email);

    // Проверка: есть ли у пользователя активированный инвайт на конкретный Demo Day
    boolean existsByDemoDayIdAndJuryUserIdAndStatus(
            UUID demoDayId, UUID juryUserId, JuryInviteStatus status);

    // Все Demo Day где этот пользователь является жюри (для профиля)
    List<JuryInvite> findByJuryUserIdAndStatus(UUID juryUserId, JuryInviteStatus status);
}