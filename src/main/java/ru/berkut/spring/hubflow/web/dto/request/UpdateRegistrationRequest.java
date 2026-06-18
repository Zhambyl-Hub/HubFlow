package ru.berkut.spring.hubflow.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateRegistrationRequest(@NotNull boolean registrationOpen) {}
