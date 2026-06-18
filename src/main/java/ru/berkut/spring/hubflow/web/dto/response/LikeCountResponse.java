package ru.berkut.spring.hubflow.web.dto.response;

import java.util.UUID;

public record LikeCountResponse(UUID teamId, String teamName, long likeCount, boolean likedByMe) {}

