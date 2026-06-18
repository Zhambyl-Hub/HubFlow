package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.ProgressStatus;

import java.util.List;
import java.util.Map;

public record ProgressMatrixResponse(
        List<TeamProgressResponse> teams,
        List<CheckpointSummaryResponse> checkpoints,
        Map<String, ProgressStatus> matrix
) {
}