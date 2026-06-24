package ru.berkut.spring.hubflow.web.dto.request;

public record UpdateParticipantMaterialsRequest(
    String pitchDeckUrl,
    String videoUrl
) {}
