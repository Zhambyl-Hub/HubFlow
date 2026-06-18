package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJuryInviteRequest(
        @NotBlank @Size(max = 255) String name,
        @Email    @Size(max = 255) String email
) {}