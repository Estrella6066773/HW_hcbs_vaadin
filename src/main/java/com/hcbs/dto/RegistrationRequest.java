package com.hcbs.dto;

public record RegistrationRequest(
        String username,
        String email,
        String password,
        String confirmPassword,
        String fullName,
        String phone
) {
}
