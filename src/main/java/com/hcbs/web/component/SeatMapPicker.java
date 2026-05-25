package com.hcbs.web.component;

import com.hcbs.config.SeatGridFormat;
import com.hcbs.dto.SeatMapSeat;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Site-themed seat map: 10 rows × (3 + aisle + 4 + aisle + 3) columns.
 */
public class SeatMapPicker extends VerticalLayout {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    private final Div showtimeStrip = new Div();
    private final Div seatGridHost = new Div();
    private final Span selectionHint = new Span("Click available seats to select");

    private final Map<Long, SeatMapSeat> seatById = new LinkedHashMap<>();
    private final Map<String, SeatMapSeat> seatByNumber = new LinkedHashMap<>();
    private final Set<Long> selectedIds = new LinkedHashSet<>();
    private Consumer<Set<SeatMapSeat>> selectionListener;

    public SeatMapPicker() {
        addClassName("booking-seat-map");
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        showtimeStrip.addClassName("booking-showtime-strip");

        Div screenBlock = new Div();
        screenBlock.addClassName("booking-screen-block");
        Span screenLabel = new Span("Screen");
        screenLabel.addClassName("booking-screen-label");
        screenBlock.add(screenLabel);

        seatGridHost.addClassName("booking-seat-grid");
        selectionHint.addClassName("selection-summary");
        selectionHint.addClassName("booking-selection-hint");

        add(showtimeStrip, screenBlock, seatGridHost, selectionHint, buildLegend());
    }

    public void setShowtimes(List<ShowtimeOption> showtimes, Long activeShowingId,
                             Consumer<Long> onShowtimeSelected) {
        showtimeStrip.removeAll();
        if (showtimes == null || showtimes.size() <= 1) {
            showtimeStrip.setVisible(false);
            return;
        }
        showtimeStrip.setVisible(true);
        for (ShowtimeOption option : showtimes) {
            Button chip = new Button(option.label());
            chip.addClassName("booking-showtime-chip");
            if (option.showingId().equals(activeShowingId)) {
                chip.addClassName("booking-showtime-chip-active");
            }
            chip.addClickListener(e -> {
                if (onShowtimeSelected != null) {
                    onShowtimeSelected.accept(option.showingId());
                }
            });
            showtimeStrip.add(chip);
        }
    }

    public void setSeats(List<SeatMapSeat> seats) {
        seatById.clear();
        seatByNumber.clear();
        selectedIds.clear();
        if (seats != null) {
            seats.stream()
                    .sorted((a, b) -> SeatGridFormat.compareSeatNumbers(a.seatNumber(), b.seatNumber()))
                    .forEach(seat -> {
                        seatById.put(seat.seatId(), seat);
                        seatByNumber.put(seat.seatNumber().toUpperCase(), seat);
                    });
        }
        renderGrid();
        fireSelectionChanged();
    }

    public Set<SeatMapSeat> getSelectedSeats() {
        Set<SeatMapSeat> selected = new LinkedHashSet<>();
        for (Long id : selectedIds) {
            SeatMapSeat seat = seatById.get(id);
            if (seat != null) {
                selected.add(seat);
            }
        }
        return selected;
    }

    public void clearSelection() {
        selectedIds.clear();
        renderGrid();
        fireSelectionChanged();
    }

    public void setSelectionListener(Consumer<Set<SeatMapSeat>> listener) {
        this.selectionListener = listener;
    }

    private void renderGrid() {
        seatGridHost.removeAll();
        if (seatByNumber.isEmpty()) {
            Span empty = new Span("No seats configured for this screen.");
            empty.addClassName("booking-seat-empty");
            seatGridHost.add(empty);
            return;
        }
        for (int row = 1; row <= SeatGridFormat.ROWS; row++) {
            HorizontalLayout rowLayout = new HorizontalLayout();
            rowLayout.addClassName("booking-seat-row");
            rowLayout.setSpacing(false);
            rowLayout.setPadding(false);
            rowLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            rowLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            rowLayout.setWidth(null);
            rowLayout.getElement().getStyle().set("width", "fit-content");

            Span rowLabel = new Span(String.valueOf(row));
            rowLabel.addClassName("booking-row-label");
            rowLayout.add(rowLabel);

            for (int col = 1; col <= SeatGridFormat.COLS; col++) {
                if (SeatGridFormat.isAisleAfterColumn(col - 1)) {
                    rowLayout.add(aisleGap());
                }
                String key = SeatGridFormat.seatNumber(row, col);
                SeatMapSeat seat = seatByNumber.get(key);
                if (seat != null) {
                    rowLayout.add(seatButton(seat));
                } else {
                    rowLayout.add(missingSeat());
                }
            }
            seatGridHost.add(rowLayout);
        }
    }

    private Div aisleGap() {
        Div aisle = new Div();
        aisle.addClassName("booking-seat-aisle");
        return aisle;
    }

    private Div missingSeat() {
        Div gap = new Div();
        gap.addClassName("booking-seat-spacer");
        return gap;
    }

    private Button seatButton(SeatMapSeat seat) {
        String label = String.valueOf(SeatGridFormat.columnFromSeatNumber(seat.seatNumber()));
        Button button = new Button(label);
        button.addClassName("booking-seat");
        applySeatStyle(button, seat);
        button.getElement().setAttribute("data-seat-id", String.valueOf(seat.seatId()));
        button.setTooltipText(seat.seatNumber() + " · £" + seat.ticketPrice());
        if (seat.available()) {
            button.addClickListener(e -> toggleSeat(seat.seatId(), button));
        }
        return button;
    }

    private void toggleSeat(Long seatId, Button button) {
        SeatMapSeat seat = seatById.get(seatId);
        if (seat == null || !seat.available()) {
            return;
        }
        if (selectedIds.contains(seatId)) {
            selectedIds.remove(seatId);
        } else {
            selectedIds.add(seatId);
        }
        applySeatStyle(button, seat);
        fireSelectionChanged();
    }

    private void applySeatStyle(Button button, SeatMapSeat seat) {
        button.removeClassName("booking-seat-available");
        button.removeClassName("booking-seat-selected");
        button.removeClassName("booking-seat-reserved");
        if (!seat.available()) {
            button.addClassName("booking-seat-reserved");
            button.setEnabled(false);
        } else if (selectedIds.contains(seat.seatId())) {
            button.addClassName("booking-seat-selected");
            button.setEnabled(true);
        } else {
            button.addClassName("booking-seat-available");
            button.setEnabled(true);
        }
    }

    private void fireSelectionChanged() {
        if (selectionListener != null) {
            selectionListener.accept(getSelectedSeats());
        }
        int count = selectedIds.size();
        if (count == 0) {
            selectionHint.setText("Click available seats to select");
            return;
        }
        BigDecimal total = getSelectedSeats().stream()
                .map(SeatMapSeat::ticketPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String seats = getSelectedSeats().stream()
                .map(SeatMapSeat::seatNumber)
                .sorted(SeatGridFormat::compareSeatNumbers)
                .collect(Collectors.joining(", "));
        selectionHint.setText(count + " seat(s) · £" + total + " — " + seats);
    }

    private Div buildLegend() {
        Div legend = new Div();
        legend.addClassName("booking-seat-legend");
        legend.add(legendItem("booking-legend-available", "Available"));
        legend.add(legendItem("booking-legend-reserved", "Reserved"));
        legend.add(legendItem("booking-legend-selected", "Selected"));
        return legend;
    }

    private Div legendItem(String swatchClass, String label) {
        Div item = new Div();
        item.addClassName("booking-legend-item");
        Div swatch = new Div();
        swatch.addClassName("booking-legend-swatch");
        swatch.addClassName(swatchClass);
        item.add(swatch, new Span(label));
        return item;
    }

    /** Showtime chip for the horizontal strip. */
    public record ShowtimeOption(Long showingId, LocalTime startTime) {
        public String label() {
            return startTime == null ? "—" : TIME.format(startTime);
        }
    }
}
