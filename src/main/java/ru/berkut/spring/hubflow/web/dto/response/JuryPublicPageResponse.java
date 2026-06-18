package ru.berkut.spring.hubflow.web.dto.response;

import java.util.List;

public record JuryPublicPageResponse(
        DemoDayResponse     demoDay,
        List<TeamResponse> teams
) {}
