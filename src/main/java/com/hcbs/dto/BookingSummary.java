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
        BigDecimal cancellationCharge
) {
    public String toDetailText() {
        return """
                Reference: %s
                Film: %s
                Date: %s
                Time: %s
                Total cost: £%s
                Status: %s
                Can cancel now: %s
                Cancellation charge: £%s
                """.formatted(
                bookingReference,
                filmTitle,
                showDate,
                startTime,
                totalCost,
                status,
                canCancel ? "Yes" : "No",
                cancellationCharge == null ? "0.00" : cancellationCharge);
    }
}
