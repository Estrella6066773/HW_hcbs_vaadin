package com.hcbs.dto;

import java.time.LocalDate;

/**
 * Filter criteria for the Film Listing page (city, cinema, date, film title).
 * 按城市、影院、日期、片名筛场次
 */
public record ShowingListingFilter(Long cityId, Long cinemaId, LocalDate date, String filmTitle) {

    public static ShowingListingFilter of(Long cityId, Long cinemaId, LocalDate date, String filmTitle) {
        String title = filmTitle == null || filmTitle.isBlank() ? null : filmTitle.trim();
        return new ShowingListingFilter(cityId, cinemaId, date, title);
    }

    public boolean isEmpty() {
        return cityId == null && cinemaId == null && date == null
                && (filmTitle == null || filmTitle.isBlank());
    }
}
