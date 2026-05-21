package com.hcbs.dto;

public record CityOption(Long cityId, String name) {
    @Override
    public String toString() {
        return name;
    }
}
