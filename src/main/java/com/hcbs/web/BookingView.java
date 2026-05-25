package com.hcbs.web;

import com.hcbs.dto.BookingReceipt;
import com.hcbs.dto.BookingShowingContext;
import com.hcbs.dto.SeatMapSeat;
import com.hcbs.dto.ShowingOption;
import com.hcbs.dto.UserOption;
import com.hcbs.security.CurrentUserService;
import com.hcbs.service.booking.BookingService;
import com.hcbs.web.component.PageHero;
import com.hcbs.web.component.SeatMapPicker;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Route(value = "booking", layout = MainLayout.class)
@PageTitle("Booking")
@AnonymousAllowed
public class BookingView extends VerticalLayout implements BeforeEnterObserver {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    private final ComboBox<ShowingOption> showing = new ComboBox<>("Showing");
    private final ComboBox<UserOption> customer = new ComboBox<>("Customer");
    private final SeatMapPicker seatPicker = new SeatMapPicker();
    private final TextArea receipt = new TextArea("Booking receipt");
    private final Span filmTitle = new Span();
    private final Span sessionMeta = new Span();
    private final Div pickerHost = new Div();

    private Long activeShowingId;
    private boolean workspaceBuilt;

    public BookingView(BookingService bookingService, CurrentUserService currentUserService) {
        this.bookingService = bookingService;
        this.currentUserService = currentUserService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long preset = ShowingFilterQuery.showingIdFrom(event.getLocation().getQueryParameters()).orElse(null);

        if (!currentUserService.isAuthenticated()) {
            Map<String, List<String>> params = new HashMap<>();
            String target = "booking";
            if (preset != null) {
                target += "?showingId=" + preset;
            }
            params.put("redirect", List.of(target));
            event.forwardTo(LoginView.class, new QueryParameters(params));
            return;
        }
        if (!workspaceBuilt) {
            buildWorkspace();
            workspaceBuilt = true;
        }
        loadShowing(preset);
    }

    private void buildWorkspace() {
        boolean employeeDesk = currentUserService.isEmployee();
        customer.setVisible(employeeDesk);
        if (employeeDesk) {
            customer.setItems(bookingService.listCustomersForDesk());
            customer.setRequired(true);
            customer.setPlaceholder("Select customer for on-behalf booking");
        }

        showing.setItems(bookingService.listBookableShowings());
        showing.setVisible(false);
        showing.addValueChangeListener(e -> {
            ShowingOption value = e.getValue();
            if (value != null) {
                switchShowing(value.showingId());
            }
        });

        receipt.setWidthFull();
        receipt.setMinHeight("220px");
        receipt.setPlaceholder("Confirmed booking details will appear here.");
        receipt.addClassName("receipt-field");

        filmTitle.addClassName("booking-session-title");
        sessionMeta.addClassName("booking-session-meta");

        pickerHost.addClassName("booking-picker-host");
        pickerHost.setWidthFull();
        pickerHost.add(filmTitle, sessionMeta, seatPicker);

        Button confirm = new Button("Confirm booking", e -> confirm());
        confirm.addClassName("primary-action");

        Div formPanel = new Div(
                sectionTitle("Select seats", employeeDesk
                        ? "10×10 grid (3 + aisle + 4 + aisle + 3). Confirm for the selected customer."
                        : "10×10 grid (3 + aisle + 4 + aisle + 3). Click seats to select."),
                customer,
                showing,
                pickerHost,
                confirm
        );
        formPanel.addClassName("surface-panel");
        formPanel.addClassName("booking-form");

        Div policyCard = new Div(
                metricLine("Layout", "100 seats per screen · rows 1–10"),
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

        add(new PageHero(
                employeeDesk ? "Employee desk" : "Self-service",
                employeeDesk ? "Ticket desk" : "Book tickets",
                "Pick a showtime, then choose seats on the grid."), workspace);
    }

    private void loadShowing(Long showingId) {
        if (showingId == null) {
            showing.setVisible(true);
            activeShowingId = null;
            pickerHost.setVisible(false);
            filmTitle.setText("Choose a showing to load the seat map.");
            sessionMeta.setText("");
            return;
        }
        Optional<BookingShowingContext> context = bookingService.findBookingContext(showingId);
        if (context.isEmpty()) {
            Notification.show("Selected showing is no longer available for booking");
            showing.setVisible(true);
            pickerHost.setVisible(false);
            return;
        }
        applyContext(context.get());
    }

    private void applyContext(BookingShowingContext context) {
        activeShowingId = context.showingId();
        showing.setVisible(false);
        pickerHost.setVisible(true);

        filmTitle.setText(context.filmTitle());
        sessionMeta.setText(context.cinemaName() + " · " + context.screenLabel() + " · " + context.showDate());

        List<SeatMapPicker.ShowtimeOption> chips = context.showtimes().stream()
                .map(slot -> new SeatMapPicker.ShowtimeOption(slot.showingId(), slot.startTime()))
                .toList();
        seatPicker.setShowtimes(chips, context.showingId(), this::switchShowing);
        seatPicker.setSeats(bookingService.listSeatMap(context.showingId()));
    }

    private void switchShowing(Long showingId) {
        if (showingId == null || showingId.equals(activeShowingId)) {
            return;
        }
        bookingService.findBookingContext(showingId).ifPresentOrElse(ctx -> {
            applyContext(ctx);
            getUI().ifPresent(ui -> ui.getPage().getHistory().replaceState(
                    null, "booking?showingId=" + showingId));
        }, () -> Notification.show("Showing is no longer available"));
    }

    private void confirm() {
        if (!currentUserService.isAuthenticated()) {
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            return;
        }
        if (activeShowingId == null) {
            Notification.show("Please choose a showing first");
            return;
        }
        Set<SeatMapSeat> selected = seatPicker.getSelectedSeats();
        if (selected.isEmpty()) {
            Notification.show("Please select at least one seat on the map");
            return;
        }
        try {
            Long customerId = customer.isVisible() && customer.getValue() != null
                    ? customer.getValue().userId()
                    : null;
            if (customer.isVisible() && customerId == null) {
                Notification.show("Please select a customer");
                return;
            }
            List<Long> seatIds = new ArrayList<>();
            selected.forEach(seat -> seatIds.add(seat.seatId()));
            BookingReceipt bookingReceipt = bookingService.createBooking(activeShowingId, seatIds, customerId);
            receipt.setValue(bookingReceipt.toReceiptText());
            seatPicker.clearSelection();
            seatPicker.setSeats(bookingService.listSeatMap(activeShowingId));
            Notification.show("Booking confirmed");
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
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
