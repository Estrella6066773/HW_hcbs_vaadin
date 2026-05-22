package com.hcbs.dto;

public record FilmCardDto(
        Long filmId,
        String title,
        String posterUrl,
        String genre,
        String ageRating,
        double rating
) {
}
