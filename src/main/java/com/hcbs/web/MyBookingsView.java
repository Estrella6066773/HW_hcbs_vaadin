package com.hcbs.web;

import com.hcbs.dto.CustomerBookingRow;
import com.hcbs.model.BookingStatus;
import com.hcbs.service.cancellation.CancellationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My bookings")
@RolesAllowed("CUSTOMER")
public class MyBookingsView extends VerticalLayout {

    private final CancellationService cancellationService;
    private final Grid<CustomerBookingRow> grid = new Grid<>(CustomerBookingRow.class, false);

    public MyBookingsView(CancellationService cancellationService) {
        this.cancellationService = cancellationService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        grid.addColumn(CustomerBookingRow::bookingReference).setHeader("Reference").setFlexGrow(1);
        grid.addColumn(CustomerBookingRow::filmTitle).setHeader("Film").setFlexGrow(2);
        grid.addColumn(CustomerBookingRow::showDate).setHeader("Date");
        grid.addColumn(CustomerBookingRow::startTime).setHeader("Time");
        grid.addColumn(CustomerBookingRow::numberOfTickets).setHeader("Tickets");
        grid.addColumn(CustomerBookingRow::totalCost).setHeader("Total");
        grid.addColumn(row -> row.status().name()).setHeader("Status");
        grid.addComponentColumn(row -> {
            Button cancel = new Button("Cancel", event -> cancel(row.bookingReference()));
            boolean cancellable = row.status() == BookingStatus.CONFIRMED && row.canCancel();
            cancel.setEnabled(cancellable);
            cancel.addClassName(cancellable ? "danger-action" : "inactive-action");
            return cancel;
        }).setHeader("Action");
        grid.setItems(cancellationService.listAccessibleBookings());
        grid.setWidthFull();

        Div panel = new Div(sectionTitle("Your orders", "Only bookings on your account are listed. Cancel before show day."), grid);
        panel.addClassName("surface-panel");

        add(pageHero("My bookings", "View and cancel orders you placed."), panel);
    }

    private void cancel(String reference) {
        try {
            cancellationService.cancelBooking(reference);
            grid.setItems(cancellationService.listAccessibleBookings());
            Notification.show("Booking cancelled: " + reference);
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private Div pageHero(String heading, String copy) {
        Span badge = new Span("Self-service");
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
}
