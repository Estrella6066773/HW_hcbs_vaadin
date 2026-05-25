package com.hcbs.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Showing metadata and same-day showtimes for the cinema seat picker. */
public record BookingShowingContext(
        Long showingId,
        String filmTitle,
        String cinemaName,
        String screenLabel,
        LocalDate showDate,
        LocalTime startTime,
        List<ShowingTimeSlot> showtimes) {
}
