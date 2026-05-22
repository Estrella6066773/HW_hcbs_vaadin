package com.hcbs.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FilmAdminRow {
    private Long filmId;
    private String title;
    private String genre;
    private String ageRating;
    private double rating;
    private int durationMinutes;
    private String posterUrl;

    public FilmAdminRow(Long filmId, String title, String genre, String ageRating,
                        double rating, int durationMinutes, String posterUrl) {
        this.filmId = filmId;
        this.title = title;
        this.genre = genre;
        this.ageRating = ageRating;
        this.rating = rating;
        this.durationMinutes = durationMinutes;
        this.posterUrl = posterUrl;
    }
}
