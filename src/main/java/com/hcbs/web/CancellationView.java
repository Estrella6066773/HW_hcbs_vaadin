package com.hcbs.web;

import com.hcbs.dto.BookingSummary;
import com.hcbs.service.cancellation.CancellationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "cancellation", layout = MainLayout.class)
@PageTitle("Cancellation")
public class CancellationView extends VerticalLayout {
    private final CancellationService cancellationService;
    private final TextField reference = new TextField("Booking reference");
    private final TextArea details = new TextArea("Booking details");

    public CancellationView(CancellationService cancellationService) {
        this.cancellationService = cancellationService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        details.setWidthFull();
        details.setMinHeight("220px");
        details.setPlaceholder("Search a booking reference to inspect cancellation eligibility.");
        details.addClassName("receipt-field");

        Button find = new Button("Find booking", event -> findBooking());
        Button cancel = new Button("Cancel booking", event -> cancelBooking());
        find.addClassName("secondary-action");
        cancel.addClassName("danger-action");

        HorizontalLayout actions = new HorizontalLayout(reference, find, cancel);
        actions.addClassName("filter-bar");
        actions.setDefaultVerticalComponentAlignment(Alignment.END);

        Div lookupPanel = new Div(sectionTitle("Refund Desk", "Find a booking, inspect the rule outcome, and record cancellation charges."), actions, details);
        lookupPanel.addClassName("surface-panel");

        Div rulePanel = new Div(
                sectionTitle("Cancellation Rules", "The demo keeps the policy visible for assessment and operator clarity."),
                ruleLine("Before showing day", "Allowed"),
                ruleLine("Same day", "Rejected"),
                ruleLine("Cancellation charge", "50% of total booking cost")
        );
        rulePanel.addClassName("surface-panel");
        rulePanel.addClassName("rule-panel");

        HorizontalLayout workspace = new HorizontalLayout(lookupPanel, rulePanel);
        workspace.addClassName("booking-workspace");
        workspace.setWidthFull();

        add(pageHero("Cancellation", "Handle customer booking changes with visible policy checks and receipt-style output."), workspace);
    }

    private void findBooking() {
        try {
            BookingSummary summary = cancellationService.findBookingSummary(reference.getValue());
            details.setValue(summary.toDetailText());
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private void cancelBooking() {
        try {
            BookingSummary summary = cancellationService.cancelBooking(reference.getValue());
            details.setValue(summary.toDetailText());
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private Div pageHero(String heading, String copy) {
        Span badge = new Span("Refund Control");
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
