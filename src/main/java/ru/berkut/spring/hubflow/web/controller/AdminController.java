package ru.berkut.spring.hubflow.web.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.AdminService;
import ru.berkut.spring.hubflow.web.dto.request.ChangeSystemRoleRequest;
import ru.berkut.spring.hubflow.web.dto.response.PageResponse;
import ru.berkut.spring.hubflow.web.dto.response.SystemRoleHistoryResponse;
import ru.berkut.spring.hubflow.web.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // GET /hubflow/api/v1/admin/users
    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        Page<User> users = adminService.getUsers(page, size);
        List<UserResponse> content = users.getContent().stream().map(this::toUserResponse).toList();
        return ResponseEntity.ok(new PageResponse<>(content, page, size,
                users.getTotalElements(), users.getTotalPages()));
    }

    // GET /hubflow/api/v1/admin/users/mentors
    @GetMapping("/users/mentors")
    public ResponseEntity<List<UserResponse>> getMentors(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminService.getMentors(principal)
                .stream().map(this::toUserResponse).toList());
    }

    // PATCH /hubflow/api/v1/admin/users/{id}/role  — Этап 1
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable UUID id,
            @RequestBody ChangeSystemRoleRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        User updated = adminService.changeSystemRole(id, req.role(), principal);
        return ResponseEntity.ok(toUserResponse(updated));
    }

    // GET /hubflow/api/v1/admin/users/{id}/role-history
    @GetMapping("/users/{id}/role-history")
    public ResponseEntity<List<SystemRoleHistoryResponse>> getRoleHistory(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminService.getRoleHistory(id, principal)
                .stream().map(h -> new SystemRoleHistoryResponse(
                        h.getId(), h.getUser().getId(),
                        h.getUser().getFirstName() + " " + h.getUser().getLastName(),
                        h.getOldRole(), h.getNewRole(),
                        h.getChangedBy().getFirstName() + " " + h.getChangedBy().getLastName(),
                        h.getChangedAt()))
                .toList());
    }

    private UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFirstName(),
                u.getLastName(), u.getPhone(), u.getAvatarUrl(),
                u.getIsActive(), u.getCreatedAt());
    }
}
