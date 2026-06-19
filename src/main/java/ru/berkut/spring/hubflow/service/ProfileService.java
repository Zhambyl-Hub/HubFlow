package ru.berkut.spring.hubflow.service;

import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.exception.NotFoundException;
import ru.berkut.spring.hubflow.repository.UserRepository;
import ru.berkut.spring.hubflow.security.UserPrincipal;
import ru.berkut.spring.hubflow.web.dto.request.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    // GET /profile/me — информация о текущем пользователе
    @Transactional(readOnly = true)
    public User getMyProfile(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> NotFoundException.of("User", principal.getId()));
    }

    // PATCH /profile/me — частичное обновление своих данных
    @Transactional
    public User updateMyProfile(UpdateProfileRequest req, UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> NotFoundException.of("User", principal.getId()));

        if (req.firstName() != null) user.setFirstName(req.firstName());
        if (req.lastName() != null)  user.setLastName(req.lastName());
        if (req.phone() != null)     user.setPhone(req.phone());
        if (req.avatarUrl() != null) user.setAvatarUrl(req.avatarUrl());

        return userRepository.save(user);
    }
}
