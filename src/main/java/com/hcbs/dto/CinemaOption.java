package com.hcbs.dto;

public record CinemaOption(Long cinemaId, String name) {
    @Override
    public String toString() {
        return name;
    }
}
