package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record ShowResultsRequest (
        @NotNull boolean show_results
){}
