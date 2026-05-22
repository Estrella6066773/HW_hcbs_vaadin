package com.hcbs.dto;

import com.hcbs.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record BookingSummary(
        String bookingReference,
        String filmTitle,
        LocalDate showDate,
        LocalTime startTime,
        BigDecimal totalCost,
        BookingStatus status,
        boolean canCancel,
        BigDecimal cancellationCharge,
        String customerName,
        String bookedByName
) {
    public String toDetailText() {
        return """
                Reference: %s
                Customer: %s
                Booked by: %s
                Film: %s
                Date: %s
                Time: %s
                Total cost: £%s
                Status: %s
                Can cancel now: %s
                Cancellation charge: £%s
                """.formatted(
                bookingReference,
                customerName,
                bookedByName,
                filmTitle,
                showDate,
                startTime,
                totalCost,
                status,
                canCancel ? "Yes" : "No",
                cancellationCharge == null ? "0.00" : cancellationCharge);
    }
}
