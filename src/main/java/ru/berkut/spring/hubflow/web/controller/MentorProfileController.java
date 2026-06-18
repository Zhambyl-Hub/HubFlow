package ru.berkut.spring.hubflow.web.controller;

import ru.berkut.spring.hubflow.entity.MentorProfile;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.service.MentorProfileService;
import ru.berkut.spring.hubflow.web.dto.request.UpdateMentorProfileRequest;
import ru.berkut.spring.hubflow.web.dto.response.MentorProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 каталог менторов.
 */
@RestController
@RequestMapping("/mentors")
@RequiredArgsConstructor
public class MentorProfileController {

    private final MentorProfileService mentorProfileService;

    // GET /hubflow/api/v1/mentors
    @GetMapping
    public ResponseEntity<List<MentorProfileResponse>> getAll() {
        return ResponseEntity.ok(mentorProfileService.getAll()
            .stream().map(this::toResponse).toList());
    }

    // GET /hubflow/api/v1/mentors/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<MentorProfileResponse> getById(@PathVariable UUID userId) {
        return ResponseEntity.ok(toResponse(mentorProfileService.getByUserId(userId)));
    }

    // GET /hubflow/api/v1/mentors/me
    @GetMapping("/me")
    public ResponseEntity<MentorProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(mentorProfileService.getMyProfile(principal)));
    }

    // PATCH /hubflow/api/v1/mentors/me
    @PatchMapping("/me")
    public ResponseEntity<MentorProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateMentorProfileRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        MentorProfile profile = mentorProfileService.updateMyProfile(
            new MentorProfileService.UpdateProfileRequest(
                req.bio(), req.expertise(), req.linkedinUrl(),
                req.avatarUrl(), req.isVisible()), principal);
        return ResponseEntity.ok(toResponse(profile));
    }

    private MentorProfileResponse toResponse(MentorProfile p) {
        return new MentorProfileResponse(
            p.getId(), p.getUser().getId(),
            p.getUser().getFirstName(), p.getUser().getLastName(),
            p.getUser().getEmail(), p.getBio(), p.getExpertise(),
            p.getLinkedinUrl(), p.getAvatarUrl(), p.getIsVisible(),
            p.getUpdatedAt());
    }
}
