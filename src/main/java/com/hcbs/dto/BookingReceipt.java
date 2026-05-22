package com.hcbs.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BookingReceipt(
        String bookingReference,
        String filmName,
        LocalDate filmDate,
        LocalTime showingTime,
        int screenNumber,
        int numberOfTickets,
        String seatNumbers,
        BigDecimal totalCost,
        LocalDateTime bookingDateTime,
        String customerName,
        String bookedByName
) {
    public String toReceiptText() {
        return """
                Booking reference: %s
                Customer: %s
                Booked by: %s
                Film name: %s
                Film date: %s
                Showing time: %s
                Screen #: %s
                Number of tickets: %s
                Seat numbers: %s
                Total booking cost: £%s
                Booking date: %s
                """.formatted(
                bookingReference,
                customerName,
                bookedByName,
                filmName,
                filmDate,
                showingTime,
                screenNumber,
                numberOfTickets,
                seatNumbers,
                totalCost,
                bookingDateTime);
    }
}
