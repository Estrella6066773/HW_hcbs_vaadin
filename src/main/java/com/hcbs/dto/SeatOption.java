package com.hcbs.dto;

import com.hcbs.model.SeatArea;

import java.math.BigDecimal;

public record SeatOption(Long seatId, String seatNumber, SeatArea seatArea, BigDecimal ticketPrice) {
    @Override
    public String toString() {
        return seatNumber + " (£" + ticketPrice + ")";
    }
}
