package com.hcbs.dto;

import com.hcbs.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CustomerBookingRow(
        String bookingReference,
        String filmTitle,
        LocalDate showDate,
        LocalTime startTime,
        int numberOfTickets,
        BigDecimal totalCost,
        BookingStatus status,
        boolean canCancel
) {
}
