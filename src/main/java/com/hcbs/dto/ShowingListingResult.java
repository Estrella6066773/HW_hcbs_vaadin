package com.hcbs.dto;

import java.util.List;

public record ShowingListingResult(List<ShowingRow> showings, long availableSeats) {
}
