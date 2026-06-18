package ru.berkut.spring.hubflow.web.dto.response;

import java.util.UUID;

public record VoteResultResponse(
    int  rank,
    UUID teamId,
    String teamName,
    long totalScore
) {}
