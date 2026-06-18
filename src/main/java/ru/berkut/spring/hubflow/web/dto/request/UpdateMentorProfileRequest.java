package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateMentorProfileRequest(
    String  bio,
    String  expertise,
    @Size(max = 500) String linkedinUrl,
    @Size(max = 500) String avatarUrl,
    Boolean isVisible
) {}
