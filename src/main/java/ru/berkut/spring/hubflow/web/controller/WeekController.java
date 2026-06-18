package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.entity.Checkpoint;
import ru.berkut.spring.hubflow.entity.Week;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.WeekService;
import ru.berkut.spring.hubflow.web.dto.request.CreateCheckpointRequest;
import ru.berkut.spring.hubflow.web.dto.request.CreateWeekRequest;
import ru.berkut.spring.hubflow.web.dto.response.CheckpointResponse;
import ru.berkut.spring.hubflow.web.dto.response.WeekResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cohorts/{cohortId}/weeks")
@RequiredArgsConstructor
public class WeekController {

    private final WeekService weekService;

    // GET /hubflow/api/v1/cohorts/{cohortId}/weeks
    @GetMapping
    public ResponseEntity<List<WeekResponse>> getWeeks(
            @PathVariable UUID cohortId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<WeekResponse> weeks = weekService.getByCohort(cohortId, principal)
            .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(weeks);
    }

    // POST /hubflow/api/v1/cohorts/{cohortId}/weeks
    @PostMapping
    public ResponseEntity<WeekResponse> createWeek(
            @PathVariable UUID cohortId,
            @Valid @RequestBody CreateWeekRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Week week = weekService.create(new WeekService.CreateWeekRequest(
            cohortId, req.weekNumber(), req.title(),
            req.goal(), req.startDate(), req.endDate()), principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(week));
    }

    // POST /hubflow/api/v1/cohorts/{cohortId}/weeks/{weekId}/checkpoints
    @PostMapping("/{weekId}/checkpoints")
    public ResponseEntity<CheckpointResponse> createCheckpoint(
            @PathVariable UUID cohortId,
            @PathVariable UUID weekId,
            @Valid @RequestBody CreateCheckpointRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Checkpoint cp = weekService.createCheckpoint(new WeekService.CreateCheckpointRequest(
            weekId, req.title(), req.description(), req.required(), req.orderIndex()), principal);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CheckpointResponse(cp.getId(), cp.getTitle(),
                cp.getDescription(), cp.getIsRequired(), cp.getOrderIndex()));
    }

    private WeekResponse toResponse(Week w) {
        List<CheckpointResponse> checkpoints = weekService.getCheckpoints(w.getId()).stream()
            .map(c -> new CheckpointResponse(c.getId(), c.getTitle(),
                c.getDescription(), c.getIsRequired(), c.getOrderIndex()))
            .toList();
        return new WeekResponse(w.getId(), w.getWeekNumber(), w.getTitle(),
            w.getGoal(), w.getStartDate(), w.getEndDate(), checkpoints);
    }
}
