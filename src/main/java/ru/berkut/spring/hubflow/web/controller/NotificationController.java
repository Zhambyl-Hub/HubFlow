package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.NotificationService;
import ru.berkut.spring.hubflow.web.dto.response.NotificationResponse;
import ru.berkut.spring.hubflow.web.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /hubflow/api/v1/notifications?page=0&size=20
    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        var pageResult = notificationService.getForUser(principal, page, size);
        var content = pageResult.getContent().stream()
            .map(n -> new NotificationResponse(n.getId(), n.getType(), n.getTitle(),
                n.getBody(), n.getMetadata(), n.getIsRead(), n.getSentAt(), n.getReadAt()))
            .toList();
        return ResponseEntity.ok(new PageResponse<>(content, page, size,
            pageResult.getTotalElements(), pageResult.getTotalPages()));
    }

    // GET /hubflow/api/v1/notifications/unread-count
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(principal)));
    }

    // PATCH /hubflow/api/v1/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markRead(id, principal);
        return ResponseEntity.noContent().build();
    }
}
