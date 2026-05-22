package com.hcbs.web;

import com.hcbs.dto.FilmCardDto;
import com.hcbs.dto.FilmCatalogFilter;
import com.hcbs.dto.ShowingRow;
import com.hcbs.service.search.HcbsSearchService;
import com.hcbs.web.component.AdditiveShowingFilterPanel;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
public class FilmRecommendView extends VerticalLayout {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final HcbsSearchService searchService;
    private final AdditiveShowingFilterPanel filterPanel;
    private final Div searchHub = new Div();
    private final Div metricsPanel = new Div();
    private final Span resultCount = new Span("0 showings");
    private final Span seatCount = new Span("0 seats open");
    private final Div posterGrid = new Div();
    private final Div showingResultsPanel = new Div();
    private final Grid<ShowingRow> showingGrid = new Grid<>(ShowingRow.class, false);

    public FilmRecommendView(HcbsSearchService searchService) {
        this.searchService = searchService;
        this.filterPanel = new AdditiveShowingFilterPanel(searchService, this::runSearch, this::showPosters);

        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        Div hero = pageHero(
                "Home",
                "Browse film posters below, or search showtimes by city, cinema, date, and title."
        );

        Div resultCard = metricCard("Filtered sessions", resultCount, "Matching showings in the current search");
        Div seatCard = metricCard("Seat inventory", seatCount, "Available seats across those sessions");
        Div ruleCard = metricCard("Booking window", new Span("7 days"), "Future bookings are limited by policy");
        metricsPanel.add(resultCard, seatCard, ruleCard);
        metricsPanel.addClassName("metric-grid");
        metricsPanel.setVisible(false);

        searchHub.addClassName("home-search-hub");
        searchHub.add(filterPanel, metricsPanel);

        posterGrid.addClassName("film-poster-grid");
        posterGrid.setWidthFull();

        configureShowingGrid();
        showingResultsPanel.addClassName("grid-panel");
        showingResultsPanel.add(showingGrid);
        showingResultsPanel.setVisible(false);

        hero.setWidthFull();
        add(hero, searchHub, posterGrid, showingResultsPanel);
        showPosters();
    }

    private void configureShowingGrid() {
        showingGrid.addComponentColumn(row -> {
            RouterLink link = new RouterLink(row.filmTitle(), FilmDetailView.class, row.filmId());
            link.addClassName("home-grid-film-link");
            return link;
        }).setHeader("Film").setWidth("160px").setFlexGrow(1);
        showingGrid.addColumn(ShowingRow::description).setHeader("Description").setWidth("220px").setFlexGrow(2);
        showingGrid.addColumn(ShowingRow::actors).setHeader("Actors").setWidth("200px").setFlexGrow(1);
        showingGrid.addColumn(ShowingRow::genre).setHeader("Genre").setWidth("100px").setFlexGrow(0);
        showingGrid.addColumn(ShowingRow::ageRating).setHeader("Age").setWidth("80px").setFlexGrow(0);
        showingGrid.addColumn(ShowingRow::cinemaName).setHeader("Cinema").setWidth("200px").setFlexGrow(1);
        showingGrid.addColumn(ShowingRow::screenNumber).setHeader("Screen").setWidth("90px").setFlexGrow(0);
        showingGrid.addColumn(row -> row.showDate().format(DATE_FORMAT)).setHeader("Date").setWidth("130px").setFlexGrow(0);
        showingGrid.addColumn(row -> row.startTime().format(TIME_FORMAT)).setHeader("Start").setWidth("90px").setFlexGrow(0);
        showingGrid.addColumn(row -> row.endTime().format(TIME_FORMAT)).setHeader("End").setWidth("90px").setFlexGrow(0);
        showingGrid.addColumn(ShowingRow::timeBand).setHeader("Band").setWidth("120px").setFlexGrow(0);
        showingGrid.addColumn(ShowingRow::availableSeats).setHeader("Available seats").setWidth("140px").setFlexGrow(0);
        showingGrid.setSizeFull();
        showingGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        showingGrid.addClassName("cinema-grid");
    }

    private void runSearch() {
        posterGrid.setVisible(false);
        showingResultsPanel.setVisible(true);
        metricsPanel.setVisible(true);
        filterPanel.hideBrowseSummary();

        var result = searchService.searchHomeShowings(filterPanel.getFilter());
        showingGrid.setItems(result.showings());
        resultCount.setText(result.showings().size() + " showings");
        seatCount.setText(result.availableSeats() + " seats open");
    }

    private void showPosters() {
        posterGrid.setVisible(true);
        showingResultsPanel.setVisible(false);
        metricsPanel.setVisible(false);

        var films = searchService.searchFilms(FilmCatalogFilter.of(""));
        posterGrid.removeAll();
        films.forEach(film -> posterGrid.add(createPosterCard(film)));
        filterPanel.setBrowseSummary(films.size());
    }

    private RouterLink createPosterCard(FilmCardDto film) {
        Image poster = new Image(film.posterUrl(), film.title() + " poster");
        poster.addClassName("film-poster-image");

        H2 title = new H2(film.title());
        title.addClassName("film-poster-title");

        Span meta = new Span(film.genre() + " · " + film.ageRating() + " · ★ " + film.rating());
        meta.addClassName("film-poster-meta");

        RouterLink link = new RouterLink();
        link.setRoute(FilmDetailView.class, film.filmId());
        link.addClassName("film-poster-card");
        link.add(poster, title, meta);
        return link;
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

    private Div pageHero(String heading, String copy) {
        Span badge = new Span("Horizon Cinemas");
        badge.addClassName("eyebrow");
        H2 title = new H2(heading);
        Paragraph description = new Paragraph(copy);
        Div hero = new Div(badge, title, description);
        hero.addClassName("page-hero");
        return hero;
    }
}
