package ru.berkut.spring.hubflow.web.dto;

import java.util.List;
import java.util.UUID;

public record TeamResult(
        int    rank,
        UUID teamId,
        String teamName,
        long   totalScore,
        long   likeCount,
        List<CriteriaScore> breakdown
) {}
