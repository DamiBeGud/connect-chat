package com.connectchat.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequest(
    @NotNull
    @Positive
    Long phoneNumber,

    @NotBlank
    @Size(max = 100)
    String firstName,

    @NotBlank
    @Size(max = 100)
    String lastName,

    @Size(max = 100)
    String nickname,

    @NotNull
    @Past
    LocalDate dateOfBirth,

    @NotBlank
    @Size(max = 100)
    String country
) {}
