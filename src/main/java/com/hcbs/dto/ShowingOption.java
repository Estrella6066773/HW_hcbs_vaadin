package com.hcbs.dto;

public record ShowingOption(Long showingId, String label) {
    @Override
    public String toString() {
        return label;
    }
}
