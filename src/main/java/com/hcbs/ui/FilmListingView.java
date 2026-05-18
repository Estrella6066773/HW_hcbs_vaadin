package com.hcbs.ui;

import com.hcbs.model.Cinema;
import com.hcbs.model.City;
import com.hcbs.model.Showing;
import com.hcbs.service.FilmListingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Film Listing")
public class FilmListingView extends VerticalLayout {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final FilmListingService filmListingService;
    private final ComboBox<City> city = new ComboBox<>("City");
    private final ComboBox<Cinema> cinema = new ComboBox<>("Cinema");
    private final DatePicker date = new DatePicker("Date");
    private final TextField title = new TextField("Film title");
    private final Grid<Showing> grid = new Grid<>(Showing.class, false);
    private final Span resultCount = new Span("0 showings");
    private final Span seatCount = new Span("0 seats open");

    public FilmListingView(FilmListingService filmListingService) {
        this.filmListingService = filmListingService;
        setSizeFull();
        addClassName("page-view");

        city.setItems(filmListingService.getAllCities());
        city.addValueChangeListener(event -> cinema.setItems(filmListingService.getCinemasByCity(event.getValue())));
        cinema.setItems(filmListingService.getCinemasByCity(null));
        date.setValue(LocalDate.now().plusDays(3));

        Button search = new Button("Search", event -> refresh());
        search.addClassName("primary-action");
        HorizontalLayout filters = new HorizontalLayout(city, cinema, date, title, search);
        filters.addClassName("filter-bar");
        filters.setDefaultVerticalComponentAlignment(Alignment.END);

        Div hero = pageHero(
                "Now Showing",
                "Browse live schedules by city, cinema, date, and title before opening the ticket desk."
        );

        Div resultCard = metricCard("Filtered sessions", resultCount, "Matching showings in the current search");
        Div seatCard = metricCard("Seat inventory", seatCount, "Available seats across those sessions");
        Div ruleCard = metricCard("Booking window", new Span("7 days"), "Future bookings are limited by policy");
        Div metrics = new Div(resultCard, seatCard, ruleCard);
        metrics.addClassName("metric-grid");

        Div filterPanel = new Div(filters);
        filterPanel.addClassName("surface-panel");

        grid.addColumn(showing -> showing.getFilm().getTitle()).setHeader("Film").setWidth("180px").setFlexGrow(1);
        grid.addColumn(showing -> filmListingService.getFilmActors(showing.getFilm())).setHeader("Actors").setWidth("250px").setFlexGrow(1);
        grid.addColumn(showing -> showing.getFilm().getGenre()).setHeader("Genre").setWidth("120px").setFlexGrow(0);
        grid.addColumn(showing -> showing.getFilm().getAgeRating()).setHeader("Age").setWidth("90px").setFlexGrow(0);
        grid.addColumn(showing -> showing.getScreen().getCinema().getName()).setHeader("Cinema").setWidth("230px").setFlexGrow(1);
        grid.addColumn(showing -> showing.getScreen().getScreenNumber()).setHeader("Screen").setWidth("100px").setFlexGrow(0);
        grid.addColumn(showing -> showing.getShowDate().format(DATE_FORMAT)).setHeader("Date").setWidth("150px").setFlexGrow(0);
        grid.addColumn(showing -> showing.getStartTime().format(TIME_FORMAT)).setHeader("Start").setWidth("100px").setFlexGrow(0);
        grid.addColumn(showing -> showing.getEndTime().format(TIME_FORMAT)).setHeader("End").setWidth("100px").setFlexGrow(0);
        grid.addColumn(Showing::getTimeBand).setHeader("Band").setWidth("130px").setFlexGrow(0);
        grid.addColumn(filmListingService::countAvailableSeats).setHeader("Available seats").setWidth("150px").setFlexGrow(0);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassName("cinema-grid");

        Div gridPanel = new Div(grid);
        gridPanel.addClassName("grid-panel");

        add(hero, metrics, filterPanel, gridPanel);
        refresh();
    }

    private void refresh() {
        List<Showing> showings = filmListingService.searchShowings(city.getValue(), cinema.getValue(), date.getValue(), title.getValue());
        grid.setItems(showings);
        resultCount.setText(showings.size() + " showings");
        long availableSeats = showings.stream().mapToLong(filmListingService::countAvailableSeats).sum();
        seatCount.setText(availableSeats + " seats open");
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
