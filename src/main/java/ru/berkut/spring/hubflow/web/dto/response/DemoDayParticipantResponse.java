package ru.berkut.spring.hubflow.web.dto.response;

import java.util.UUID;

public record DemoDayParticipantResponse(
    UUID   id,
    UUID   teamId,
    String teamName,
    Integer presentationOrder,
    String pitchDeckUrl,
    String videoUrl
) {}
