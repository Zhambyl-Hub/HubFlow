package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.*;
import ru.berkut.spring.hubflow.enums.NotificationType;
import ru.berkut.spring.hubflow.enums.VotingStatus;
import ru.berkut.spring.hubflow.exception.AccessDeniedException;
import ru.berkut.spring.hubflow.exception.BadRequestException;
import ru.berkut.spring.hubflow.exception.ConflictException;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.*;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berkut.spring.hubflow.web.dto.response.DemoCriteriaResponse;
import ru.berkut.spring.hubflow.web.dto.response.DemoDayParticipantResponse;
import ru.berkut.spring.hubflow.web.dto.response.DemoDayResponse;
import ru.berkut.spring.hubflow.web.dto.request.AddCriteriaRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemoDayService {
    private final DemoCriteriaRepository        demoCriteriaRepository;
    private final DemoDayRepository             demoDayRepository;
    private final DemoDayParticipantRepository  participantRepository;
    private final VoteRepository                voteRepository;
    private final UserRepository                userRepository;
    private final CohortRepository              cohortRepository;
    private final TeamRepository                teamRepository;
    private final CohortService                 cohortService;
    private final NotificationService           notificationService;

    public Optional<DemoDayResponse> getByCohort(UUID cohortId) {
        return demoDayRepository.findByCohortId(cohortId).map(demoDay -> {

            List<DemoCriteriaResponse> criteriaList = demoCriteriaRepository.findByDemoDayIdOrderByOrderIndex(demoDay.getId()).stream().map(
                    demoCriteria -> new DemoCriteriaResponse(demoCriteria.getId(),demoCriteria.getTitle(),demoCriteria.getDescription(),demoCriteria.getMaxScore(),demoCriteria.getOrderIndex())
            ).collect(Collectors.toList());

            return new DemoDayResponse(
                    demoDay.getId(),
                    cohortId,
                    demoDay.getEventDate(),
                    demoDay.getDescription(),
                    demoDay.getVotingStatus(),
                    demoDay.getShowResultsPublicly(),
                    demoDay.getVotingOpensAt(),
                    demoDay.getVotingClosesAt(),
                    criteriaList
            );
        });
    }


    public record CreateDemoDayRequest(UUID cohortId, Instant eventDate, String description) {}
    public record VoteRequest(UUID demoDayId, UUID teamId, UUID criterionId, int score) {}

    @Transactional
    public DemoDay create(CreateDemoDayRequest req, UserPrincipal principal) {
        cohortService.requireAdmin(principal.getId());

        if (demoDayRepository.findByCohortId(req.cohortId()).isPresent()) {
            throw new ConflictException("Demo Day already exists for this cohort");
        }
        Cohort cohort = cohortRepository.getReferenceById(req.cohortId());
        return demoDayRepository.save(DemoDay.builder()
                .cohort(cohort)
                .eventDate(req.eventDate())
                .description(req.description())
                .build());
    }

    @Transactional
    public DemoDay openVoting(UUID demoDayId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());

        if (day.getVotingStatus() != VotingStatus.CLOSED) {
            throw new BadRequestException("Voting already started or finished");
        }
        day.setVotingStatus(VotingStatus.OPEN);
        day.setVotingOpensAt(Instant.now());

        // Уведомление всем участникам когорты
        notificationService.notifyCohort(
                day.getCohort().getId(),
                NotificationType.VOTE_OPEN,
                "Голосование открыто",
                "Demo Day начался — отдайте свой голос!",
                null
        );
        return demoDayRepository.save(day);
    }

    @Transactional
    public DemoDay closeVoting(UUID demoDayId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());
        day.setVotingStatus(VotingStatus.FINISHED);
        day.setVotingClosesAt(Instant.now());
        return demoDayRepository.save(day);
    }
    @Transactional
    public DemoDay showResults(UUID demoDayId,boolean showValue, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());
        day.setShowResultsPublicly(showValue);
        return demoDayRepository.save(day);
    }

    @Transactional
    public Vote vote(VoteRequest req, UserPrincipal principal) {
        DemoDay day = getDemoDay(req.demoDayId());

        if (day.getVotingStatus() != VotingStatus.OPEN) {
            throw new BadRequestException("Voting is not open");
        }
        if (voteRepository.existsByDemoDayIdAndVoterIdAndTeamIdAndCriterionId(
                req.demoDayId(), principal.getId(), req.teamId(), req.criterionId())) {
            throw new ConflictException("Already voted for this team/criterion");
        }

        DemoCriteria criterion = demoCriteriaRepository.findById(req.criterionId())
                .orElseThrow(() -> NotFoundException.of("DemoCriteria", req.criterionId()));
        if (!criterion.getDemoDay().getId().equals(req.demoDayId())) {
            throw new BadRequestException("Criterion does not belong to this Demo Day");
        }

        User voter = userRepository.getReferenceById(principal.getId());
        Team team  = teamRepository.getReferenceById(req.teamId());

        return voteRepository.save(Vote.builder()
                .demoDay(day)
                .voter(voter)
                .team(team)
                .criterion(criterion)
                .score(req.score())
                .build());
    }

    @Transactional(readOnly = true)
    public List<Object[]> getResults(UUID demoDayId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        if (!day.getShowResultsPublicly()) {
            cohortService.requireAdminOrMentor(principal.getId(), day.getCohort().getId());
        }
        return voteRepository.getScoresByDemoDay(demoDayId);
    }

    // ─────────────────── Критерии оценки ───────────────────

    @Transactional
    public DemoCriteria addCriteria(UUID demoDayId, AddCriteriaRequest req,
                                    UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());

        DemoCriteria criteria = DemoCriteria.builder()
                .demoDay(day)
                .title(req.title())
                .description(req.description())
                .maxScore(req.maxScore())
                .orderIndex(req.orderIndex())
                .build();
        return demoCriteriaRepository.save(criteria);
    }

    @Transactional(readOnly = true)
    public List<DemoCriteria> listCriteria(UUID demoDayId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        if (!principal.isAdmin()) {
            cohortService.checkMembership(principal.getId(), day.getCohort().getId());
        }
        return demoCriteriaRepository.findByDemoDayIdOrderByOrderIndex(demoDayId);
    }

    @Transactional
    public void deleteCriteria(UUID demoDayId, UUID criterionId, UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());

        DemoCriteria criteria = demoCriteriaRepository.findById(criterionId)
                .orElseThrow(() -> NotFoundException.of("DemoCriteria", criterionId));
        if (!criteria.getDemoDay().getId().equals(demoDayId)) {
            throw new BadRequestException("Критерий не принадлежит данному Demo Day");
        }
        demoCriteriaRepository.delete(criteria);
    }

    // ─────────────────── Участники Demo Day ───────────────────

    @Transactional
    public DemoDayParticipantResponse addParticipant(UUID demoDayId, UUID teamId,
                                                     Integer presentationOrder,
                                                     UserPrincipal principal) {
        DemoDay day = getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());

        if (participantRepository.existsByDemoDayIdAndTeamId(demoDayId, teamId)) {
            throw new ConflictException("Team is already a participant of this Demo Day");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> NotFoundException.of("Team", teamId));

        DemoDayParticipant participant = participantRepository.save(DemoDayParticipant.builder()
                .demoDay(day)
                .team(team)
                .presentationOrder(presentationOrder)
                .build());

        return toParticipantResponse(participant);
    }

    @Transactional
    public DemoDayParticipantResponse updateMaterials(UUID demoDayId, UUID teamId,
                                                      String pitchDeckUrl, String videoUrl,
                                                      UserPrincipal principal) {
        cohortService.checkMembership(principal.getId(), getDemoDay(demoDayId).getCohort().getId());

        DemoDayParticipant participant = participantRepository
                .findByDemoDayIdAndTeamId(demoDayId, teamId)
                .orElseThrow(() -> new NotFoundException("Participant not found"));

        if (pitchDeckUrl != null) participant.setPitchDeckUrl(pitchDeckUrl);
        if (videoUrl     != null) participant.setVideoUrl(videoUrl);
        participantRepository.save(participant);
        return toParticipantResponse(participant);
    }

    @Transactional
    public void removeParticipant(UUID demoDayId, UUID teamId, UserPrincipal principal) {
        getDemoDay(demoDayId);
        cohortService.requireAdmin(principal.getId());

        DemoDayParticipant participant = participantRepository
                .findByDemoDayIdAndTeamId(demoDayId, teamId)
                .orElseThrow(() -> new NotFoundException("Participant not found"));

        participantRepository.delete(participant);
    }

    @Transactional(readOnly = true)
    public List<DemoDayParticipantResponse> listParticipants(UUID demoDayId) {
        return participantRepository.findByDemoDayIdOrderByPresentationOrder(demoDayId)
                .stream().map(this::toParticipantResponse).toList();
    }

    private DemoDayParticipantResponse toParticipantResponse(DemoDayParticipant p) {
        return new DemoDayParticipantResponse(
                p.getId(), p.getTeam().getId(), p.getTeam().getName(),
                p.getPresentationOrder(), p.getPitchDeckUrl(), p.getVideoUrl());
    }

    private DemoDay getDemoDay(UUID id) {
        return demoDayRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("DemoDay", id));
    }

}
