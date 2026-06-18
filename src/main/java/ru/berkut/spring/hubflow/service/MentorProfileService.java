package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.MentorProfile;
import ru.berkut.spring.hubflow.repository.*;
import ru.berkut.spring.hubflow.exception.*;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MentorProfileService {

    private final MentorProfileRepository    mentorProfileRepository;
    private final UserRepository             userRepository;

    public record UpdateProfileRequest(String bio, String expertise,
                                       String linkedinUrl, String avatarUrl,
                                       Boolean isVisible) {}

    // ── Этап 3: каталог менторов ────────────────────────────────────────

    // GET /mentors — все видимые менторы системы
    @Transactional(readOnly = true)
    public List<MentorProfile> getAll() {
        return mentorProfileRepository.findByIsVisibleTrue();
    }

    // GET /mentors/{userId}
    @Transactional(readOnly = true)
    public MentorProfile getByUserId(UUID userId) {
        return mentorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> NotFoundException.of("MentorProfile", userId));
    }

    // GET /cohorts/{id}/mentors — менторы привязанные к когорте
    @Transactional(readOnly = true)
    public List<MentorProfile> getByCohort(UUID cohortId) {
        return mentorProfileRepository.findByCohortId(cohortId);
    }

    // ── Свой профиль ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MentorProfile getMyProfile(UserPrincipal principal) {
        requireMentor(principal);
        return mentorProfileRepository.findByUserId(principal.getId())
            .orElseThrow(() -> new NotFoundException("Mentor profile not found"));
    }

    @Transactional
    public MentorProfile updateMyProfile(UpdateProfileRequest req, UserPrincipal principal) {
        requireMentor(principal);
        MentorProfile profile = mentorProfileRepository.findByUserId(principal.getId())
            .orElseThrow(() -> new NotFoundException("Mentor profile not found"));

        if (req.bio() != null)         profile.setBio(req.bio());
        if (req.expertise() != null)   profile.setExpertise(req.expertise());
        if (req.linkedinUrl() != null) profile.setLinkedinUrl(req.linkedinUrl());
        if (req.avatarUrl() != null)   profile.setAvatarUrl(req.avatarUrl());
        if (req.isVisible() != null)   profile.setIsVisible(req.isVisible());

        return mentorProfileRepository.save(profile);
    }

    private void requireMentor(UserPrincipal principal) {
        if (!principal.isMentor() && !principal.isAdmin()) {
            throw new AccessDeniedException("MENTOR role required");
        }
    }
}
