package ru.berkut.spring.hubflow.web.dto.response;

import java.util.UUID;

public record TeamProgressResponse(
        UUID id,
        String name,
        double progress
) {
}
