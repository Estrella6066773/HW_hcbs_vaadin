package com.hcbs.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 10×10 seat grid: rows 1–10, columns 1–10 (layout 3 + aisle + 4 + aisle + 3). */
public final class SeatGridFormat {

    public static final int ROWS = 10;
    public static final int COLS = 10;
    public static final int SEATS_PER_SCREEN = ROWS * COLS;

    private static final Pattern SEAT_NUMBER = Pattern.compile("^R(\\d{1,2})C(\\d{1,2})$");

    private SeatGridFormat() {
    }

    public static String seatNumber(int row, int column) {
        if (row < 1 || row > ROWS || column < 1 || column > COLS) {
            throw new IllegalArgumentException("Seat outside grid: row=" + row + ", column=" + column);
        }
        return "R%02dC%02d".formatted(row, column);
    }

    public static int rowFromSeatNumber(String seatNumber) {
        return parse(seatNumber).row();
    }

    public static int columnFromSeatNumber(String seatNumber) {
        return parse(seatNumber).column();
    }

    public static int compareSeatNumbers(String left, String right) {
        SeatCoordinate a = parse(left);
        SeatCoordinate b = parse(right);
        int byRow = Integer.compare(a.row(), b.row());
        return byRow != 0 ? byRow : Integer.compare(a.column(), b.column());
    }

    public static boolean isAisleAfterColumn(int column) {
        return column == 3 || column == 7;
    }

    private static SeatCoordinate parse(String seatNumber) {
        if (seatNumber == null) {
            throw new IllegalArgumentException("Seat number is required");
        }
        Matcher matcher = SEAT_NUMBER.matcher(seatNumber.trim().toUpperCase());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid seat number: " + seatNumber);
        }
        return new SeatCoordinate(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }

    private record SeatCoordinate(int row, int column) {
    }
}
