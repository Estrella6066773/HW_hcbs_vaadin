package com.hcbs.dto;

import com.hcbs.model.TimeBand;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShowingRow(
        Long showingId,
        Long filmId,
        String filmTitle,
        String description,
        String actors,
        String genre,
        String ageRating,
        String cinemaName,
        int screenNumber,
        LocalDate showDate,
        LocalTime startTime,
        LocalTime endTime,
        TimeBand timeBand,
        long availableSeats
) {
}
