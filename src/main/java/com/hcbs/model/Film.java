package com.hcbs.model;

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

    private String title;
    private String description;
    private String genre;
    private String ageRating;
    private double rating;
    private int durationMinutes;

    public Film(String title, String description, String genre, String ageRating, double rating, int durationMinutes) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.ageRating = ageRating;
        this.rating = rating;
        this.durationMinutes = durationMinutes;
    }

    @Override
    public String toString() {
        return title;
    }
}
