package com.hcbs.dto;

import java.util.List;

public record FilmDetailDto(
        Long filmId,
        String title,
        String posterUrl,
        String description,
        String genre,
        String ageRating,
        double rating,
        int durationMinutes,
        String actors,
        List<ShowingRow> upcomingShowings
) {
}
