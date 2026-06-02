package com.hcbs.web.cancellation;

import com.hcbs.dto.CustomerBookingRow;
import com.hcbs.model.BookingStatus;
import com.hcbs.security.CurrentUserService;
import com.hcbs.service.cancellation.CancellationService;
import com.hcbs.util.PhoneNumbers;
import com.hcbs.web.component.PageHero;
import com.hcbs.web.shell.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "cancellation", layout = MainLayout.class)
@PageTitle("Cancellation")
@RolesAllowed({"BOOKING_STAFF", "ADMIN"})
public class CancellationView extends VerticalLayout {

    private final CancellationService cancellationService;
    private final CurrentUserService currentUserService;

    private final ComboBox<String> customerPhone = new ComboBox<>("Customer phone");
    private final Grid<CustomerBookingRow> bookingsGrid = new Grid<>(CustomerBookingRow.class, false);

    private String activePhone;

    public CancellationView(CancellationService cancellationService, CurrentUserService currentUserService) {
        this.cancellationService = cancellationService;
        this.currentUserService = currentUserService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        customerPhone.setWidthFull();
        customerPhone.setClearButtonVisible(true);
        customerPhone.setAllowCustomValue(true);
        customerPhone.setItems(query -> {
            String filter = query.getFilter().orElse("");
            return cancellationService.searchPhonesWithBookings(filter).stream()
                    .skip(query.getOffset())
                    .limit(query.getLimit());
        });
        customerPhone.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                activePhone = event.getValue();
                loadBookings(event.getValue());
            } else {
                activePhone = null;
                bookingsGrid.setItems();
            }
        });
        customerPhone.addCustomValueSetListener(event -> {
            activePhone = PhoneNumbers.normalize(event.getDetail());
            loadBookings(event.getDetail());
        });

        bookingsGrid.addColumn(CustomerBookingRow::bookingReference).setHeader("Reference").setFlexGrow(1);
        bookingsGrid.addColumn(CustomerBookingRow::filmTitle).setHeader("Film").setFlexGrow(2);
        bookingsGrid.addColumn(CustomerBookingRow::showDate).setHeader("Date");
        bookingsGrid.addColumn(CustomerBookingRow::startTime).setHeader("Time");
        bookingsGrid.addColumn(CustomerBookingRow::numberOfTickets).setHeader("Tickets");
        bookingsGrid.addColumn(CustomerBookingRow::totalCost).setHeader("Total");
        bookingsGrid.addColumn(row -> row.status().name()).setHeader("Status");
        bookingsGrid.addComponentColumn(row -> {
            Button cancel = new Button("Cancel", event -> cancelBooking(row.bookingReference()));
            boolean cancellable = row.status() == BookingStatus.CONFIRMED && row.canCancel();
            cancel.setEnabled(cancellable);
            cancel.addClassName(cancellable ? "danger-action" : "inactive-action");
            return cancel;
        }).setHeader("Action");
        bookingsGrid.setWidthFull();
        bookingsGrid.setAllRowsVisible(true);

        Div lookupPanel = new Div(
                sectionTitle("Refund desk", "Search by phone to list bookings, then cancel the selected order."),
                customerPhone,
                bookingsGrid
        );
        lookupPanel.addClassName("surface-panel");

        Div rulePanel = new Div(
                sectionTitle("Cancellation rules", "Policy checks apply before seats are released."),
                ruleLine("Before showing day", "Allowed"),
                ruleLine("Same day", "Rejected"),
                ruleLine("Cancellation charge", "50% of total booking cost"),
                ruleLine("Signed in as", currentUserService.requireCurrentUser().getFullName())
        );
        rulePanel.addClassName("surface-panel");
        rulePanel.addClassName("rule-panel");

        HorizontalLayout workspace = new HorizontalLayout(lookupPanel, rulePanel);
        workspace.addClassName("booking-workspace");
        workspace.setWidthFull();

        add(new PageHero("Refund control", "Cancellation", "Find orders by phone and cancel eligible bookings."), workspace);
    }

    private void loadBookings(String rawPhone) {
        try {
            String phone = PhoneNumbers.normalize(rawPhone);
            if (phone == null || phone.length() < 3) {
                bookingsGrid.setItems();
                return;
            }
            bookingsGrid.setItems(cancellationService.listBookingsByPhone(phone));
        } catch (RuntimeException ex) {
            bookingsGrid.setItems();
            Notification.show(ex.getMessage());
        }
    }

    private void cancelBooking(String reference) {
        try {
            cancellationService.cancelBooking(reference);
            if (activePhone != null && !activePhone.isBlank()) {
                loadBookings(activePhone);
            } else {
                bookingsGrid.setItems();
            }
            Notification.show("Booking cancelled: " + reference);
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

    private Div ruleLine(String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.addClassName("metric-label");
        Span valueSpan = new Span(value);
        valueSpan.addClassName("policy-value");
        Div line = new Div(labelSpan, valueSpan);
        line.addClassName("policy-line");
        return line;
    }
}
