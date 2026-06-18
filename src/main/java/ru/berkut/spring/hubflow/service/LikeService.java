package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.AudienceLike;
import ru.berkut.spring.hubflow.entity.DemoDay;
import ru.berkut.spring.hubflow.entity.Team;
import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.enums.VotingStatus;
import ru.berkut.spring.hubflow.exception.BadRequestException;
import ru.berkut.spring.hubflow.exception.ConflictException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.*;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berkut.spring.hubflow.web.dto.response.LikeCountResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final AudienceLikeRepository audienceLikeRepository;
    private final DemoDayRepository      demoDayRepository;
    private final TeamRepository         teamRepository;
    private final TeamMemberRepository   teamMemberRepository;
    private final UserRepository         userRepository;

    // ─────────────────── Response records ───────────────────


    // ─────────────────── Поставить лайк ───────────────────

    @Transactional
    public void like(UUID demoDayId, UUID teamId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);

        if (day.getVotingStatus() != VotingStatus.OPEN) {
            throw new BadRequestException("Лайки принимаются только во время открытого голосования");
        }

        // Нельзя лайкать свою команду
        checkNotOwnTeam(principal.getId(), demoDayId, teamId);

        if (audienceLikeRepository.existsByDemoDayIdAndUserIdAndTeamId(
                demoDayId, principal.getId(), teamId)) {
            throw new ConflictException("Вы уже поставили лайк этой команде");
        }

        User user = userRepository.getReferenceById(principal.getId());
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Команда не найдена"));

        audienceLikeRepository.save(AudienceLike.builder()
                .demoDay(day)
                .user(user)
                .team(team)
                .build());
    }

    // ─────────────────── Убрать лайк ───────────────────

    @Transactional
    public void unlike(UUID demoDayId, UUID teamId, UserPrincipal principal) {
        AudienceLike like = audienceLikeRepository
                .findByDemoDayIdAndUserIdAndTeamId(demoDayId, principal.getId(), teamId)
                .orElseThrow(() -> new NotFoundException("Лайк не найден"));

        audienceLikeRepository.delete(like);
    }

    // ─────────────────── Получить счётчики лайков ───────────────────

    @Transactional(readOnly = true)
    public List<LikeCountResponse> getLikeCounts(UUID demoDayId, UserPrincipal principal) {
        getDemoDay(demoDayId); // проверяем что Demo Day существует

        return audienceLikeRepository.getLikeCountsByDemoDay(demoDayId)
                .stream()
                .map(row -> {
                    UUID   tId    = (UUID)   row[0];
                    String tName  = (String) row[1];
                    long   count  = ((Number) row[2]).longValue();
                    boolean likedByMe = audienceLikeRepository
                            .existsByDemoDayIdAndUserIdAndTeamId(demoDayId, principal.getId(), tId);
                    return new LikeCountResponse(tId, tName, count, likedByMe);
                })
                .toList();
    }

    // ─────────────────── helpers ───────────────────

    private DemoDay getDemoDay(UUID id) {
        return demoDayRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("DemoDay", id));
    }

    /**
     * Находим команду пользователя на этом Demo Day через участников.
     * Если пользователь состоит в команде-участнике — лайкать её нельзя.
     */
    private void checkNotOwnTeam(UUID userId, UUID demoDayId, UUID targetTeamId) {
        boolean isOwnTeam = teamMemberRepository.findByUserId(userId).stream()
                .anyMatch(tm -> tm.getTeam().getId().equals(targetTeamId));

        if (isOwnTeam) {
            throw new BadRequestException("Нельзя ставить лайк своей команде");
        }
    }
}