package com.hcbs.web;

import com.hcbs.dto.ShowingRow;
import com.hcbs.service.search.HcbsSearchService;
import com.hcbs.web.component.ShowingListingFilterPanel;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "listings", layout = MainLayout.class)
@PageTitle("Film Listing")
public class FilmListingView extends VerticalLayout {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final HcbsSearchService searchService;
    private final ShowingListingFilterPanel filterPanel;
    private final Grid<ShowingRow> grid = new Grid<>(ShowingRow.class, false);
    private final Span resultCount = new Span("0 showings");
    private final Span seatCount = new Span("0 seats open");

    public FilmListingView(HcbsSearchService searchService) {
        this.searchService = searchService;
        this.filterPanel = new ShowingListingFilterPanel(searchService, this::refresh);

        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        Div hero = pageHero(
                "Now Showing",
                "Browse live schedules by city, cinema, date, and title before opening the ticket desk."
        );

        Div resultCard = metricCard("Filtered sessions", resultCount, "Matching showings in the current search");
        Div seatCard = metricCard("Seat inventory", seatCount, "Available seats across those sessions");
        Div ruleCard = metricCard("Booking window", new Span("7 days"), "Future bookings are limited by policy");
        Div metrics = new Div(resultCard, seatCard, ruleCard);
        metrics.addClassName("metric-grid");

        configureGrid();

        Div gridPanel = new Div(grid);
        gridPanel.addClassName("grid-panel");

        add(hero, metrics, filterPanel, gridPanel);
        refresh();
    }

    private void configureGrid() {
        grid.addColumn(ShowingRow::filmTitle).setHeader("Film").setWidth("160px").setFlexGrow(1);
        grid.addColumn(ShowingRow::description).setHeader("Description").setWidth("220px").setFlexGrow(2);
        grid.addColumn(ShowingRow::actors).setHeader("Actors").setWidth("200px").setFlexGrow(1);
        grid.addColumn(ShowingRow::genre).setHeader("Genre").setWidth("100px").setFlexGrow(0);
        grid.addColumn(ShowingRow::ageRating).setHeader("Age").setWidth("80px").setFlexGrow(0);
        grid.addColumn(ShowingRow::cinemaName).setHeader("Cinema").setWidth("200px").setFlexGrow(1);
        grid.addColumn(ShowingRow::screenNumber).setHeader("Screen").setWidth("90px").setFlexGrow(0);
        grid.addColumn(row -> row.showDate().format(DATE_FORMAT)).setHeader("Date").setWidth("130px").setFlexGrow(0);
        grid.addColumn(row -> row.startTime().format(TIME_FORMAT)).setHeader("Start").setWidth("90px").setFlexGrow(0);
        grid.addColumn(row -> row.endTime().format(TIME_FORMAT)).setHeader("End").setWidth("90px").setFlexGrow(0);
        grid.addColumn(ShowingRow::timeBand).setHeader("Band").setWidth("120px").setFlexGrow(0);
        grid.addColumn(ShowingRow::availableSeats).setHeader("Available seats").setWidth("140px").setFlexGrow(0);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassName("cinema-grid");
    }

    private void refresh() {
        var result = searchService.searchShowings(filterPanel.getFilter());
        grid.setItems(result.showings());
        resultCount.setText(result.showings().size() + " showings");
        seatCount.setText(result.availableSeats() + " seats open");
    }

    private Div pageHero(String heading, String copy) {
        H2 title = new H2(heading);
        Paragraph description = new Paragraph(copy);
        Span badge = new Span("Horizon Cinemas Operations");
        badge.addClassName("eyebrow");

        Div hero = new Div(badge, title, description);
        hero.addClassName("page-hero");
        return hero;
    }

    private Div metricCard(String label, Span value, String note) {
        Span labelSpan = new Span(label);
        labelSpan.addClassName("metric-label");
        value.addClassName("metric-value");
        Span noteSpan = new Span(note);
        noteSpan.addClassName("metric-note");

        Div card = new Div(labelSpan, value, noteSpan);
        card.addClassName("metric-card");
        return card;
    }
}
