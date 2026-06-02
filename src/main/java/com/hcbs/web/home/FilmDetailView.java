package com.hcbs.web.home;

import com.hcbs.dto.FilmDetailDto;
import com.hcbs.dto.ShowingListingFilter;
import com.hcbs.dto.ShowingRow;
import com.hcbs.service.catalog.PosterResourceService;
import com.hcbs.service.search.HcbsSearchService;
import com.hcbs.web.booking.BookingView;
import com.hcbs.web.home.component.BackToHomeAction;
import com.hcbs.web.home.component.FilmPoster;
import com.hcbs.web.shell.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "film", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Film Details")
public class FilmDetailView extends VerticalLayout implements HasUrlParameter<Long> {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final HcbsSearchService searchService;
    private final PosterResourceService posterResources;
    private final VerticalLayout content = new VerticalLayout();

    public FilmDetailView(HcbsSearchService searchService, PosterResourceService posterResources) {
        this.searchService = searchService;
        this.posterResources = posterResources;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();
        add(content);
    }

    @Override
    public void setParameter(BeforeEvent event, Long filmId) {
        content.removeAll();
        if (filmId == null) {
            content.add(new Paragraph("No film selected."));
            return;
        }
        ShowingListingFilter filter = ShowingFilterQuery.fromQueryParameters(event.getLocation().getQueryParameters());
        try {
            content.add(buildDetail(searchService.getFilmDetail(filmId, filter), filter));
        } catch (IllegalArgumentException ex) {
            content.add(new Paragraph(ex.getMessage()));
            content.add(new RouterLink("Back to home", FilmRecommendView.class));
        }
    }

    private Div buildDetail(FilmDetailDto film, ShowingListingFilter filter) {
        FilmPoster poster = new FilmPoster(
                posterResources, film.posterUrl(), film.title() + " poster", "film-detail-poster");

        H2 title = new H2(film.title());
        Span meta = new Span(film.genre() + " · " + film.ageRating() + " · ★ " + film.rating()
                + " · " + film.durationMinutes() + " min");
        meta.addClassName("film-detail-meta");
        Paragraph description = new Paragraph(film.description());
        Paragraph actors = new Paragraph("Cast: " + film.actors());

        Div text = new Div(title, meta, description, actors);
        text.addClassName("film-detail-copy");

        HorizontalLayout header = new HorizontalLayout(poster, text);
        header.addClassName("film-detail-header");
        header.setWidthFull();
        header.setAlignItems(Alignment.START);

        Div actions = new Div(new BackToHomeAction(filter));
        actions.addClassName("film-detail-actions");

        if (ShowingFilterQuery.hasCriteria(filter)) {
            Span filterNote = new Span("Showtimes below match your search filters.");
            filterNote.addClassName("film-detail-filter-note");
            actions.add(filterNote);
        }

        Grid<ShowingRow> showings = new Grid<>(ShowingRow.class, false);
        showings.addColumn(ShowingRow::cinemaName).setHeader("Cinema").setFlexGrow(1);
        showings.addColumn(ShowingRow::screenNumber).setHeader("Screen").setWidth("90px");
        showings.addColumn(row -> row.showDate().format(DATE_FORMAT)).setHeader("Date").setWidth("130px");
        showings.addColumn(row -> row.startTime().format(TIME_FORMAT)).setHeader("Start").setWidth("90px");
        showings.addColumn(ShowingRow::availableSeats).setHeader("Seats").setWidth("90px");
        showings.addComponentColumn(row -> {
            Button book = new Button("Book");
            book.addClassName("primary-action");
            book.addClickListener(e -> book.getUI().ifPresent(ui -> ui.navigate(
                    BookingView.class,
                    ShowingFilterQuery.withShowingId(filter, row.showingId()))));
            return book;
        }).setHeader("").setWidth("120px").setFlexGrow(0);
        showings.setItems(film.upcomingShowings());
        showings.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        showings.setWidthFull();
        showings.setAllRowsVisible(true);

        H2 showtimesTitle = new H2("Upcoming showtimes");
        showtimesTitle.addClassName("section-heading");

        Div panel = new Div(showtimesTitle, showings);
        panel.addClassName("surface-panel");
        panel.addClassName("film-detail-showings");

        if (film.upcomingShowings().isEmpty()) {
            Paragraph empty = new Paragraph(ShowingFilterQuery.hasCriteria(filter)
                    ? "No upcoming showtimes match your current filters."
                    : "No upcoming showtimes for this film.");
            empty.addClassName("empty-state");
            panel.add(empty);
        }

        Div root = new Div(actions, header, panel);
        root.addClassName("film-detail-root");
        return root;
    }
}
