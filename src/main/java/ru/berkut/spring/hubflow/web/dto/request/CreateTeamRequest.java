package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.*;

public record CreateTeamRequest(
    @NotBlank @Size(max = 255) String name,
    String ideaDescription,
    String problem,
    @Size(max = 255) String targetSegment,
    String solution
) {}
