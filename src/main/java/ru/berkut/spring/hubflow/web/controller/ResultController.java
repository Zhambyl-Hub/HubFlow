package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.ResultService;
import ru.berkut.spring.hubflow.web.dto.TeamResult;
import ru.berkut.spring.hubflow.web.dto.response.RankingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cohorts/{cohortId}/demo-day/{demoDayId}/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    /**
     * GET /cohorts/{cohortId}/demo-day/{demoDayId}/results
     * Полный рейтинг с разбивкой по критериям. ADMIN — всегда,
     * приглашённое жюри — всегда, остальные — если включён публичный показ.
     */
    @GetMapping
    public ResponseEntity<RankingResponse> getFullRanking(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(resultService.getFullRanking(demoDayId, principal));
    }

    /**
     * GET /cohorts/{cohortId}/demo-day/{demoDayId}/results/top3
     * Топ-3 команды. Участникам видно только после завершения Demo Day
     * и выставления оценок всеми членами жюри.
     */
    @GetMapping("/top3")
    public ResponseEntity<List<TeamResult>> getTop3(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(resultService.getTop3(demoDayId, principal));
    }

    /**
     * GET /cohorts/{cohortId}/demo-day/{demoDayId}/results/winner
     * Победитель. Участникам видно только после завершения Demo Day
     * и выставления оценок всеми членами жюри.
     */
    @GetMapping("/winner")
    public ResponseEntity<TeamResult> getWinner(
            @PathVariable UUID cohortId,
            @PathVariable UUID demoDayId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(resultService.getWinner(demoDayId, principal));
    }
}
