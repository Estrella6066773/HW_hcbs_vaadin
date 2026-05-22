package com.hcbs.dto;

/**
 * Filter criteria for the Home page film catalog search (title, genre, synopsis).
 */
public record FilmCatalogFilter(String keyword) {

    public static FilmCatalogFilter of(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new FilmCatalogFilter("");
        }
        return new FilmCatalogFilter(keyword.trim());
    }

    public boolean isEmpty() {
        return keyword == null || keyword.isBlank();
    }
}
