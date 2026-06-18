package ru.berkut.spring.hubflow.web.dto.response;

import ru.berkut.spring.hubflow.enums.NotificationType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
    UUID             id,
    NotificationType type,
    String           title,
    String           body,
    Map<String,Object> metadata,
    Boolean          isRead,
    Instant          sentAt,
    Instant          readAt
) {}
