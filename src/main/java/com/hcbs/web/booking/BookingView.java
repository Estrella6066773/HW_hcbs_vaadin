package com.hcbs.web.booking;

import com.hcbs.dto.BookingReceipt;
import com.hcbs.dto.BookingShowingContext;
import com.hcbs.dto.PhoneSearchOption;
import com.hcbs.dto.SeatMapSeat;
import com.hcbs.dto.ShowingOption;
import com.hcbs.security.CurrentUserService;
import com.hcbs.service.booking.BookingService;
import com.hcbs.util.PhoneNumbers;
import com.hcbs.web.auth.LoginView;
import com.hcbs.web.booking.component.SeatMapPicker;
import com.hcbs.web.component.PageHero;
import com.hcbs.web.home.ShowingFilterQuery;
import com.hcbs.web.shell.MainLayout;
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

    private final ComboBox<PhoneSearchOption> customerPhone = new ComboBox<>("Customer phone");
    private final ComboBox<ShowingOption> showing = new ComboBox<>("Showing");
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

    private String selectedCustomerPhone;

    private void buildWorkspace() {
        boolean employeeDesk = currentUserService.isEmployee();
        customerPhone.setVisible(employeeDesk);
        if (employeeDesk) {
            configurePhoneComboBox(customerPhone, bookingService::searchCustomerPhones);
            customerPhone.addValueChangeListener(event -> {
                PhoneSearchOption value = event.getValue();
                selectedCustomerPhone = value != null ? value.phone() : null;
            });
            customerPhone.addCustomValueSetListener(event ->
                    selectedCustomerPhone = PhoneNumbers.normalize(event.getDetail()));
        }

        showing.setItems(bookingService.listBookableShowings());
        showing.setWidthFull();
        showing.setClearButtonVisible(true);
        showing.setVisible(false);
        showing.addValueChangeListener(e -> {
            ShowingOption value = e.getValue();
            if (value != null) {
                switchShowing(value.showingId());
            } else {
                resetShowingSelection();
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
                        ? "10×10 grid (3 + aisle + 4 + aisle + 3). Search customer phone, pick showing, then confirm."
                        : "10×10 grid (3 + aisle + 4 + aisle + 3). Click seats to select."),
                customerPhone,
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
                metricLine("Ownership", employeeDesk ? "Order linked by phone when account exists" : "Order belongs to you")
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

    private static void configurePhoneComboBox(
            ComboBox<PhoneSearchOption> comboBox,
            java.util.function.Function<String, List<PhoneSearchOption>> search) {
        comboBox.setWidthFull();
        comboBox.setRequiredIndicatorVisible(true);
        comboBox.setClearButtonVisible(true);
        comboBox.setAllowCustomValue(true);
        comboBox.setItemLabelGenerator(PhoneSearchOption::label);
        comboBox.setItems(query -> {
            String filter = query.getFilter().orElse("");
            return search.apply(filter).stream()
                    .skip(query.getOffset())
                    .limit(query.getLimit());
        });
    }

    private void loadShowing(Long showingId) {
        if (showingId == null) {
            showing.clear();
            showing.setVisible(true);
            resetShowingSelection();
            return;
        }
        Optional<BookingShowingContext> context = bookingService.findBookingContext(showingId);
        if (context.isEmpty()) {
            Notification.show("Selected showing is no longer available for booking");
            showing.clear();
            showing.setVisible(true);
            resetShowingSelection();
            return;
        }
        applyContext(context.get());
    }

    private void applyContext(BookingShowingContext context) {
        activeShowingId = context.showingId();
        bookingService.findBookableShowing(context.showingId()).ifPresent(showing::setValue);
        showing.setVisible(true);
        pickerHost.setVisible(true);

        filmTitle.setText(context.filmTitle());
        sessionMeta.setText(context.cinemaName() + " · " + context.screenLabel() + " · " + context.showDate());

        List<SeatMapPicker.ShowtimeOption> chips = context.showtimes().stream()
                .map(slot -> new SeatMapPicker.ShowtimeOption(slot.showingId(), slot.startTime()))
                .toList();
        seatPicker.clearSelection();
        seatPicker.setShowtimes(chips, context.showingId(), this::switchShowing);
        seatPicker.setSeats(bookingService.listSeatMap(context.showingId()));
    }

    private void resetShowingSelection() {
        activeShowingId = null;
        pickerHost.setVisible(false);
        filmTitle.setText("Choose a showing to load the seat map.");
        sessionMeta.setText("");
        seatPicker.clearSelection();
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
            String phone = resolveCustomerPhone();
            if (customerPhone.isVisible() && phone == null) {
                Notification.show("Please enter the customer phone number");
                return;
            }
            List<Long> seatIds = new ArrayList<>();
            selected.forEach(seat -> seatIds.add(seat.seatId()));
            BookingReceipt bookingReceipt = bookingService.createBooking(activeShowingId, seatIds, phone);
            receipt.setValue(bookingReceipt.toReceiptText());
            seatPicker.clearSelection();
            seatPicker.setSeats(bookingService.listSeatMap(activeShowingId));
            Notification.show("Booking confirmed");
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private String resolveCustomerPhone() {
        if (!customerPhone.isVisible()) {
            return null;
        }
        PhoneSearchOption selected = customerPhone.getValue();
        if (selected != null) {
            return selected.phone();
        }
        return selectedCustomerPhone;
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
