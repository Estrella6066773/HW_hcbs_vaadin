package com.hcbs.web;

import com.hcbs.dto.BookingReceipt;
import com.hcbs.dto.SeatOption;
import com.hcbs.dto.ShowingOption;
import com.hcbs.dto.UserOption;
import com.hcbs.model.SeatArea;
import com.hcbs.security.CurrentUserService;
import com.hcbs.service.booking.BookingService;
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
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;

@Route(value = "booking", layout = MainLayout.class)
@PageTitle("Booking")
@PermitAll
public class BookingView extends VerticalLayout {
    private final BookingService bookingService;
    private final CurrentUserService currentUserService;
    private final ComboBox<ShowingOption> showing = new ComboBox<>("Showing");
    private final ComboBox<UserOption> customer = new ComboBox<>("Customer");
    private final ComboBox<SeatArea> seatArea = new ComboBox<>("Seat area");
    private final MultiSelectComboBox<SeatOption> seats = new MultiSelectComboBox<>("Seats");
    private final TextArea receipt = new TextArea("Booking receipt");
    private final Span selectionSummary = new Span("Choose a showing and seat area to reveal available seats.");

    public BookingView(BookingService bookingService, CurrentUserService currentUserService) {
        this.bookingService = bookingService;
        this.currentUserService = currentUserService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        boolean employeeDesk = currentUserService.isEmployee();
        customer.setVisible(employeeDesk);
        if (employeeDesk) {
            customer.setItems(bookingService.listCustomersForDesk());
            customer.setRequired(true);
            customer.setPlaceholder("Select customer for on-behalf booking");
        }

        showing.setItems(bookingService.listBookableShowings());
        seatArea.setItems(SeatArea.values());
        showing.addValueChangeListener(event -> refreshSeats());
        seatArea.addValueChangeListener(event -> refreshSeats());
        seats.addValueChangeListener(event -> updateSelectionSummary());

        receipt.setWidthFull();
        receipt.setMinHeight("220px");
        receipt.setPlaceholder("Confirmed booking details will appear here.");
        receipt.addClassName("receipt-field");

        Button confirm = new Button("Confirm booking", event -> confirm());
        confirm.addClassName("primary-action");

        String heroCopy = employeeDesk
                ? "Select the customer, showing, and seats. The order is recorded under the customer account."
                : "Book seats for your own account. You can cancel orders from My bookings.";

        Div hero = pageHero(employeeDesk ? "Ticket desk" : "Book tickets", heroCopy);

        Div formPanel = new Div(
                sectionTitle("Create booking", employeeDesk
                        ? "On-behalf booking links the order to the selected customer."
                        : "Self-service booking for the signed-in customer."),
                customer,
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
                metricLine("Ownership", employeeDesk ? "Order belongs to selected customer" : "Order belongs to you")
        );
        policyCard.addClassName("ticket-policy");

        Div receiptPanel = new Div(sectionTitle("Receipt preview", "Shows customer and operator."), receipt, policyCard);
        receiptPanel.addClassName("surface-panel");
        receiptPanel.addClassName("receipt-panel");

        HorizontalLayout workspace = new HorizontalLayout(formPanel, receiptPanel);
        workspace.addClassName("booking-workspace");
        workspace.setWidthFull();

        add(hero, workspace);
    }

    private void refreshSeats() {
        ShowingOption selectedShowing = showing.getValue();
        if (selectedShowing != null && seatArea.getValue() != null) {
            seats.setItems(bookingService.listAvailableSeats(selectedShowing.showingId(), seatArea.getValue()));
        } else {
            seats.setItems(List.of());
        }
        updateSelectionSummary();
    }

    private void confirm() {
        try {
            ShowingOption selectedShowing = showing.getValue();
            if (selectedShowing == null || seats.getValue() == null || seats.getValue().isEmpty()) {
                Notification.show("Please choose a showing and at least one seat");
                return;
            }
            Long customerId = customer.isVisible() && customer.getValue() != null
                    ? customer.getValue().userId()
                    : null;
            if (customer.isVisible() && customerId == null) {
                Notification.show("Please select a customer");
                return;
            }
            List<Long> seatIds = new ArrayList<>();
            seats.getValue().forEach(option -> seatIds.add(option.seatId()));
            BookingReceipt bookingReceipt = bookingService.createBooking(
                    selectedShowing.showingId(), seatIds, customerId);
            receipt.setValue(bookingReceipt.toReceiptText());
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
        Span badge = new Span(currentUserService.isEmployee() ? "Employee desk" : "Self-service");
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
