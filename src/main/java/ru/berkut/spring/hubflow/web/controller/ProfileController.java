package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.ProfileService;
import ru.berkut.spring.hubflow.web.dto.request.UpdateProfileRequest;
import ru.berkut.spring.hubflow.web.dto.response.UserProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Профиль текущего пользователя — для любой роли (USER/MENTOR/ADMIN/JURY).
 * Заменяет каталог менторов: участникам когорты для работы с менторами
 * достаточно эндпоинта со слотами (см. MentorController), отдельный
 * каталог-профиль ментора не нужен.
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // GET /hubflow/api/v1/profile/me
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(profileService.getMyProfile(principal)));
    }

    // PATCH /hubflow/api/v1/profile/me
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(profileService.updateMyProfile(req, principal)));
    }

    private UserProfileResponse toResponse(User u) {
        return new UserProfileResponse(
                u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getPhone(), u.getAvatarUrl(), u.getSystemRole(), u.getCreatedAt());
    }
}
