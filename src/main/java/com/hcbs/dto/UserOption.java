package com.hcbs.dto;

public record UserOption(Long userId, String username, String fullName) {
    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}
