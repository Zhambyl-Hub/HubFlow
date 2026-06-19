package ru.berkut.spring.hubflow.web.dto.response;


import java.util.List;

public record ProgressMatrixResponse(
        List<TeamProgressResponse> teams,
        List<CheckpointSummaryResponse> checkpoints
) {
}