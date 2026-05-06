package com.connectchat.identity.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequest(
    @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phoneNumber,

    @NotBlank @Size(max = 100) String firstName,

    @NotBlank @Size(max = 100) String lastName,

    @Size(max = 100) String nickname,

    @NotNull @Past LocalDate dateOfBirth,

    @NotBlank @Size(max = 100) String country
) {}
