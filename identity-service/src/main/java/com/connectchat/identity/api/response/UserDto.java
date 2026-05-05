package com.connectchat.identity.api.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record UserDto(
    @NotBlank String phoneNumber,
    @NotBlank String firstName,
    @NotBlank String lastName,
    String nickname,
    @NotNull @Past LocalDate dateOfBirth,
    @NotBlank String country
) {}
