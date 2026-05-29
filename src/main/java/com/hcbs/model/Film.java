package com.hcbs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long filmId;

    @Column(nullable = false, unique = true, length = 200)
    private String title;
    private String description;
    private String genre;
    private String ageRating;
    private double rating;
    private int durationMinutes;

    /** Poster image URL persisted in DB (https or app-relative path under /images/). */
    @Column(nullable = false, length = 512)
    private String posterUrl;

    public Film(String title, String description, String genre, String ageRating, double rating, int durationMinutes) {
        this(title, description, genre, ageRating, rating, durationMinutes, com.hcbs.config.FilmPosterCatalog.FALLBACK);
    }

    public Film(String title, String description, String genre, String ageRating, double rating, int durationMinutes,
                String posterUrl) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.ageRating = ageRating;
        this.rating = rating;
        this.durationMinutes = durationMinutes;
        this.posterUrl = posterUrl;
    }

    @Override
    public String toString() {
        return title;
    }
}
