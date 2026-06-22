package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.*;
import ru.berkut.spring.hubflow.enums.JuryInviteStatus;
import ru.berkut.spring.hubflow.enums.SystemRole;
import ru.berkut.spring.hubflow.enums.VotingStatus;
import ru.berkut.spring.hubflow.exception.AccessDeniedException;
import ru.berkut.spring.hubflow.exception.BadRequestException;
import ru.berkut.spring.hubflow.exception.ConflictException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.*;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.AuthService.AuthResponse;
import ru.berkut.spring.hubflow.web.dto.request.ActivateJuryInviteRequest;
import ru.berkut.spring.hubflow.web.dto.request.CreateJuryInviteRequest;
import ru.berkut.spring.hubflow.web.dto.request.JuryVoteRequest;
import ru.berkut.spring.hubflow.web.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JuryService {

    private final JuryInviteRepository   juryInviteRepository;
    private final JuryVoteRepository     juryVoteRepository;
    private final DemoDayRepository      demoDayRepository;
    private final DemoCriteriaRepository demoCriteriaRepository;
    private final TeamRepository         teamRepository;
    private final UserRepository         userRepository;
    private final CohortService          cohortService;
    private final AuthService            authService;
    private final PasswordEncoder        passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final SecureRandom RANDOM = new SecureRandom();

    // ─────────────────── Управление приглашениями ───────────────────

    /**
     * Администратор создаёт приглашение для члена жюри.
     * Возвращает готовую ссылку вида: {baseUrl}/jury/activate?token=XXX
     */
    @Transactional
    public JuryInviteResponse createInvite(UUID demoDayId, CreateJuryInviteRequest req,
                                           UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());

        // Не даём дважды приглашать один email
        if (req.email() != null && !req.email().isBlank()) {
            if (juryInviteRepository.existsByDemoDayIdAndEmail(demoDayId, req.email())) {
                throw new ConflictException("Приглашение для " + req.email() + " уже существует");
            }
        }

        String token = generateSecureToken();

        JuryInvite invite = juryInviteRepository.save(JuryInvite.builder()
                .demoDay(day)
                .name(req.name())
                .email(req.email())
                .inviteToken(token)
                .status(JuryInviteStatus.PENDING)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build());

        return toInviteResponse(invite);
    }

    @Transactional(readOnly = true)
    public List<JuryInviteResponse> listInvites(UUID demoDayId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());

        return juryInviteRepository
                .findByDemoDayIdOrderByCreatedAtDesc(demoDayId)
                .stream()
                .map(this::toInviteResponse)
                .toList();
    }

    @Transactional
    public void revokeInvite(UUID demoDayId, UUID inviteId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());

        JuryInvite invite = juryInviteRepository.findById(inviteId)
                .orElseThrow(() -> NotFoundException.of("JuryInvite", inviteId));

        if (invite.getStatus() == JuryInviteStatus.ACTIVATED) {
            throw new BadRequestException("Нельзя отозвать уже активированное приглашение");
        }
        invite.setStatus(JuryInviteStatus.REVOKED);
        juryInviteRepository.save(invite);
    }

    // ─────────────────── Активация приглашения ───────────────────

    /**
     * Публичный эндпоинт — жюри открывает ссылку, вводит пароль и имя.
     * Создаём User(systemRole=JURY), помечаем invite как ACTIVATED,
     * сразу возвращаем JWT — жюри не нужен отдельный логин.
     */
    @Transactional
    public AuthResponse activateInvite(String rawToken, ActivateJuryInviteRequest req) {
        JuryInvite invite = juryInviteRepository.findByInviteToken(rawToken)
                .orElseThrow(() -> new NotFoundException("Ссылка недействительна или уже использована"));

        // Проверки токена
        if (invite.getStatus() != JuryInviteStatus.PENDING) {
            throw new BadRequestException(
                    switch (invite.getStatus()) {
                        case ACTIVATED -> "Приглашение уже было активировано";
                        case REVOKED   -> "Приглашение было отозвано организатором";
                        default        -> "Приглашение недействительно";
                    }
            );
        }
        if (Instant.now().isAfter(invite.getExpiresAt())) {
            invite.setStatus(JuryInviteStatus.REVOKED);
            juryInviteRepository.save(invite);
            throw new BadRequestException("Срок действия ссылки истёк");
        }

        // Определяем email: из инвайта или генерируем технический
        String email = (invite.getEmail() != null && !invite.getEmail().isBlank())
                ? invite.getEmail()
                : "jury-" + invite.getId() + "@hubflow.local";

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Пользователь с таким email уже зарегистрирован");
        }

        // Создаём пользователя с ролью JURY
        User juryUser = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .firstName(req.firstName())
                .lastName(req.lastName())
                .systemRole(SystemRole.JURY)
                .build());

        // Помечаем инвайт как активированный
        invite.setStatus(JuryInviteStatus.ACTIVATED);
        invite.setJuryUser(juryUser);
        invite.setActivatedAt(Instant.now());
        juryInviteRepository.save(invite);

        // Сразу выдаём JWT — жюри авторизован
        return authService.buildTokensPublic(juryUser);
    }

    // ─────────────────── Голосование жюри ───────────────────

    /**
     * Жюри выставляет баллы команде по критерию.
     * Проверяем: голосование открыто, score в диапазоне, не дублируем.
     */
    @Transactional
    public JuryVoteResponse vote(UUID demoDayId, JuryVoteRequest req, UserPrincipal principal) {
        requireJury(principal);

        DemoDay day = getDemoDay(demoDayId);
        if (day.getVotingStatus() != VotingStatus.OPEN) {
            throw new BadRequestException("Голосование ещё не открыто или уже завершено");
        }

        // Проверка: этот член жюри приглашён именно на этот Demo Day
        requireJuryInvitedToDay(principal.getId(), demoDayId);

        // Проверка дубля
        if (juryVoteRepository.existsByDemoDayIdAndJuryIdAndTeamIdAndCriterionId(
                demoDayId, principal.getId(), req.teamId(), req.criterionId())) {
            throw new ConflictException("Вы уже оценили эту команду по данному критерию");
        }

        DemoCriteria criterion = demoCriteriaRepository.findById(req.criterionId())
                .orElseThrow(() -> new NotFoundException("Критерий не найден"));

        if (!criterion.getDemoDay().getId().equals(demoDayId)) {
            throw new BadRequestException("Критерий не принадлежит данному Demo Day");
        }

        validateScore(req.score(), criterion.getMaxScore());

        Team team = teamRepository.findById(req.teamId())
                .orElseThrow(() -> new NotFoundException("Команда не найдена"));

        User jury = userRepository.getReferenceById(principal.getId());

        JuryVote saved = juryVoteRepository.save(JuryVote.builder()
                .demoDay(day)
                .jury(jury)
                .team(team)
                .criterion(criterion)
                .score(req.score())
                .build());

        return toVoteResponse(saved);
    }

    /**
     * Жюри просматривает свои оценки по текущему Demo Day.
     */
    @Transactional(readOnly = true)
    public List<JuryVoteResponse> getMyVotes(UUID demoDayId, UserPrincipal principal) {
        requireJury(principal);
        return juryVoteRepository
                .findByDemoDayIdAndJuryId(demoDayId, principal.getId())
                .stream()
                .map(this::toVoteResponse)
                .toList();
    }
    /**
     * Страница, которую видит член жюри после активации приглашения:
     * информация о Demo Day, критерии оценки и список команд-участниц.
     */
    @Transactional(readOnly = true)
    public JuryPublicPageResponse getPublicPage(UUID demoDayId, UserPrincipal principal) {
        requireJury(principal);
        requireJuryInvitedToDay(principal.getId(), demoDayId);

        DemoDay day = getDemoDay(demoDayId);

        List<DemoCriteriaResponse> criteria = demoCriteriaRepository
                .findByDemoDayIdOrderByOrderIndex(demoDayId)
                .stream()
                .map(c -> new DemoCriteriaResponse(
                        c.getId(), c.getTitle(), c.getDescription(), c.getMaxScore(), c.getOrderIndex()))
                .toList();

        DemoDayResponse demoDayResponse = new DemoDayResponse(
                day.getId(), day.getCohort().getId(), day.getEventDate(), day.getDescription(),
                day.getVotingStatus(), day.getShowResultsPublicly(),
                day.getVotingOpensAt(), day.getVotingClosesAt(), criteria);

        List<TeamResponse> teams = teamRepository.findByDemoDayId(demoDayId)
                .stream()
                .map(t -> new TeamResponse(
                        t.getId(), t.getName(), t.getIdeaDescription(), t.getProblem(),
                        t.getTargetSegment(), t.getSolution(), t.getStage(),
                        t.getRepoUrl(), t.getLandingUrl(), t.getPitchUrl(), t.getCreatedAt()))
                .toList();

        return new JuryPublicPageResponse(demoDayResponse, teams);
    }
    // ─────────────────── helpers ───────────────────

    private DemoDay getDemoDay(UUID id) {
        return demoDayRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("DemoDay", id));
    }

    private void requireJury(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (!user.isJury()) {
            throw new AccessDeniedException("Только члены жюри могут голосовать");
        }
    }

    /**
     * Проверяем, что у пользователя есть активированный инвайт на этот Demo Day.
     * Это предотвращает голосование жюри из другой когорты.
     */
    private void requireJuryInvitedToDay(UUID userId, UUID demoDayId) {
        boolean invited = juryInviteRepository.existsByDemoDayIdAndJuryUserIdAndStatus(
                demoDayId, userId, JuryInviteStatus.ACTIVATED);
        if (!invited) {
            throw new AccessDeniedException("Вы не приглашены на этот Demo Day");
        }
    }

    private void validateScore(int score, int maxScore) {
        if (score < 1 || score > maxScore) {
            throw new BadRequestException(
                    "Оценка должна быть от 1 до " + maxScore);
        }
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // ─────────────────── mapping ───────────────────

    public JuryInviteResponse toInviteResponse(JuryInvite inv) {
        String inviteUrl = baseUrl + "/jury/activate?token=" + inv.getInviteToken();
        return new JuryInviteResponse(
                inv.getId(),
                inv.getDemoDay().getId(),
                inv.getName(),
                inv.getEmail(),
                inv.getInviteToken(),
                inviteUrl,
                inv.getStatus(),
                inv.getJuryUser() != null ? inv.getJuryUser().getId() : null,
                inv.getExpiresAt(),
                inv.getActivatedAt(),
                inv.getCreatedAt()
        );
    }

    public JuryVoteResponse toVoteResponse(JuryVote v) {
        return new JuryVoteResponse(
                v.getId(),
                v.getTeam().getId(),
                v.getTeam().getName(),
                v.getCriterion().getId(),
                v.getCriterion().getTitle(),
                v.getScore(),
                v.getVotedAt()
        );
    }
}