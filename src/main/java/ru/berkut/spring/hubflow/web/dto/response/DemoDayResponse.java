package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.VotingStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DemoDayResponse(
    UUID          id,
    UUID          cohortId,
    Instant       eventDate,
    String        description,
    VotingStatus  votingStatus,
    Boolean       showResultsPublicly,
    Instant       votingOpensAt,
    Instant       votingClosesAt,
    List<DemoCriteriaResponse> criteria
) {}
