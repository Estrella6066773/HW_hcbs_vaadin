package com.hcbs.ui;

import com.hcbs.model.Booking;
import com.hcbs.model.Seat;
import com.hcbs.model.SeatArea;
import com.hcbs.model.Showing;
import com.hcbs.repository.ShowingRepository;
import com.hcbs.repository.UserRepository;
import com.hcbs.service.BookingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "booking", layout = MainLayout.class)
@PageTitle("Booking")
public class BookingView extends VerticalLayout {
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ComboBox<Showing> showing = new ComboBox<>("Showing");
    private final ComboBox<SeatArea> seatArea = new ComboBox<>("Seat area");
    private final MultiSelectComboBox<Seat> seats = new MultiSelectComboBox<>("Seats");
    private final TextArea receipt = new TextArea("Booking receipt");
    private final Span selectionSummary = new Span("Choose a showing and seat area to reveal available seats.");

    public BookingView(BookingService bookingService, ShowingRepository showingRepository, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        setSizeFull();
        addClassName("page-view");

        showing.setItems(showingRepository.findAll());
        showing.setItemLabelGenerator(item -> item.getFilm().getTitle() + " | " + item.getScreen() + " | " + item.getShowDate() + " " + item.getStartTime());
        seatArea.setItems(SeatArea.values());
        showing.addValueChangeListener(event -> refreshSeats());
        seatArea.addValueChangeListener(event -> refreshSeats());
        seats.addValueChangeListener(event -> updateSelectionSummary());

        receipt.setWidthFull();
        receipt.setMinHeight("220px");
        receipt.setPlaceholder("Confirmed booking details will print here as a ticket office receipt.");
        receipt.addClassName("receipt-field");

        Button confirm = new Button("Confirm booking", event -> confirm());
        confirm.addClassName("primary-action");

        Div hero = pageHero(
                "Ticket Desk",
                "Select a showing, filter the seat area, and issue a booking receipt for the demo customer."
        );

        Div formPanel = new Div(
                sectionTitle("Create booking", "The sample staff user is used automatically for this coursework demo."),
                showing,
                seatArea,
                seats,
                selectionSummary,
                confirm
        );
        formPanel.addClassName("surface-panel");
        formPanel.addClassName("booking-form");

        Div policyCard = new Div(
                metricLine("Policy", "Bookings up to 7 days ahead"),
                metricLine("Seat lock", "A seat cannot be sold twice"),
                metricLine("Pricing", "City, time band, and seat area")
        );
        policyCard.addClassName("ticket-policy");

        Div receiptPanel = new Div(sectionTitle("Receipt preview", "A compact audit trail for the operator."), receipt, policyCard);
        receiptPanel.addClassName("surface-panel");
        receiptPanel.addClassName("receipt-panel");

        HorizontalLayout workspace = new HorizontalLayout(formPanel, receiptPanel);
        workspace.addClassName("booking-workspace");
        workspace.setWidthFull();

        add(hero, workspace);
    }

    private void refreshSeats() {
        if (showing.getValue() != null && seatArea.getValue() != null) {
            seats.setItems(bookingService.getAvailableSeats(showing.getValue(), seatArea.getValue()));
        }
        updateSelectionSummary();
    }

    private void confirm() {
        try {
            if (showing.getValue() == null || seats.getValue().isEmpty()) {
                Notification.show("Please choose a showing and at least one seat");
                return;
            }
            List<Seat> selectedSeats = new ArrayList<>(seats.getValue());
            Booking booking = bookingService.createBooking(showing.getValue(), userRepository.findAll().get(0), selectedSeats);
            receipt.setValue("""
                    Booking reference: %s
                    Film name: %s
                    Film date: %s
                    Showing time: %s
                    Screen #: %s
                    Number of tickets: %s
                    Seat numbers: %s
                    Total booking cost: £%s
                    Booking date: %s
                    """.formatted(
                    booking.getBookingReference(),
                    booking.getShowing().getFilm().getTitle(),
                    booking.getShowing().getShowDate(),
                    booking.getShowing().getStartTime(),
                    booking.getShowing().getScreen().getScreenNumber(),
                    booking.getNumberOfTickets(),
                    selectedSeats.stream().map(Seat::getSeatNumber).reduce((left, right) -> left + ", " + right).orElse(""),
                    booking.getTotalCost(),
                    booking.getBookingDateTime()));
            refreshSeats();
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private void updateSelectionSummary() {
        int selected = seats.getValue() == null ? 0 : seats.getValue().size();
        if (showing.getValue() == null) {
            selectionSummary.setText("Choose a showing to start a new ticket order.");
        } else if (seatArea.getValue() == null) {
            selectionSummary.setText("Showing selected. Pick a seat area to load inventory.");
        } else {
            selectionSummary.setText(selected + " seat(s) selected in " + seatArea.getValue() + ".");
        }
        selectionSummary.addClassName("selection-summary");
    }

    private Div pageHero(String heading, String copy) {
        Span badge = new Span("Counter Mode");
        badge.addClassName("eyebrow");
        H2 title = new H2(heading);
        Paragraph description = new Paragraph(copy);

        Div hero = new Div(badge, title, description);
        hero.addClassName("page-hero");
        return hero;
    }

    private Div sectionTitle(String title, String caption) {
        H2 heading = new H2(title);
        Paragraph detail = new Paragraph(caption);
        Div wrapper = new Div(heading, detail);
        wrapper.addClassName("section-title");
        return wrapper;
    }

    private Div metricLine(String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.addClassName("metric-label");
        Span valueSpan = new Span(value);
        valueSpan.addClassName("policy-value");
        Div line = new Div(labelSpan, valueSpan);
        line.addClassName("policy-line");
        return line;
    }
}
