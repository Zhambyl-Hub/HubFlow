package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.Checkpoint;
import ru.berkut.spring.hubflow.entity.Cohort;
import ru.berkut.spring.hubflow.entity.Week;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.CheckpointRepository;
import ru.berkut.spring.hubflow.repository.CohortRepository;
import ru.berkut.spring.hubflow.repository.WeekRepository;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WeekService {

    private final WeekRepository       weekRepository;
    private final CheckpointRepository checkpointRepository;
    private final CohortRepository     cohortRepository;
    private final CohortService        cohortService;

    public record CreateWeekRequest(UUID cohortId, int weekNumber, String title,
                                    String goal, LocalDate startDate, LocalDate endDate) {}
    public record CreateCheckpointRequest(UUID weekId, String title,
                                          String description, boolean required, int orderIndex) {}

    @Transactional(readOnly = true)
    public List<Week> getByCohort(UUID cohortId, UserPrincipal principal) {
        cohortService.checkMembership(principal.getId(), cohortId);
        return weekRepository.findByCohortIdOrderByWeekNumber(cohortId);
    }

    @Transactional(readOnly = true)
    public List<Checkpoint> getCheckpoints(UUID weekId) {
        return checkpointRepository.findByWeekIdOrderByOrderIndex(weekId);
    }

    @Transactional
    public Week create(CreateWeekRequest req, UserPrincipal principal) {
        cohortService.requireAdmin(principal.getId(), req.cohortId());
        Cohort cohort = cohortRepository.getReferenceById(req.cohortId());
        return weekRepository.save(Week.builder()
            .cohort(cohort).weekNumber(req.weekNumber()).title(req.title())
            .goal(req.goal()).startDate(req.startDate()).endDate(req.endDate())
            .build());
    }

    @Transactional
    public Checkpoint createCheckpoint(CreateCheckpointRequest req, UserPrincipal principal) {
        Week week = weekRepository.findById(req.weekId())
            .orElseThrow(() -> NotFoundException.of("Week", req.weekId()));
        cohortService.requireAdmin(principal.getId(), week.getCohort().getId());
        return checkpointRepository.save(Checkpoint.builder()
            .week(week).title(req.title()).description(req.description())
            .isRequired(req.required()).orderIndex(req.orderIndex())
            .build());
    }
}
