package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.VotingStatus;
import ru.berkut.spring.hubflow.service.ResultService;
import ru.berkut.spring.hubflow.web.dto.TeamResult;

import java.util.List;
import java.util.UUID;

public record RankingResponse(
        UUID demoDayId,
        VotingStatus votingStatus,
        List<TeamResult> teams
) {}
