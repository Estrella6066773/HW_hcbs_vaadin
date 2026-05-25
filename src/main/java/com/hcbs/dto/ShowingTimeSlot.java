package com.hcbs.dto;

import java.time.LocalTime;

public record ShowingTimeSlot(Long showingId, LocalTime startTime) {
}
