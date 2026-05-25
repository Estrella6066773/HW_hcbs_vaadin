package com.hcbs.dto;

import com.hcbs.model.SeatArea;

import java.math.BigDecimal;

/** One seat cell for the visual booking map. */
public record SeatMapSeat(
        Long seatId,
        String seatNumber,
        SeatArea seatArea,
        BigDecimal ticketPrice,
        boolean available) {
}
