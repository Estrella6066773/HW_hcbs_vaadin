package com.hcbs.web;

import com.hcbs.dto.FilmDetailDto;
import com.hcbs.dto.ShowingRow;
import com.hcbs.service.catalog.FilmCatalogService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "film", layout = MainLayout.class)
@PageTitle("Film Details")
public class FilmDetailView extends VerticalLayout implements HasUrlParameter<Long> {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final FilmCatalogService filmCatalogService;
    private final VerticalLayout content = new VerticalLayout();

    public FilmDetailView(FilmCatalogService filmCatalogService) {
        this.filmCatalogService = filmCatalogService;
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
        try {
            content.add(buildDetail(filmCatalogService.getFilmDetail(filmId)));
        } catch (IllegalArgumentException ex) {
            content.add(new Paragraph(ex.getMessage()));
            content.add(new RouterLink("Back to home", FilmRecommendView.class));
        }
    }

    private Div buildDetail(FilmDetailDto film) {
        Image poster = new Image(film.posterUrl(), film.title() + " poster");
        poster.addClassName("film-detail-poster");

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

        Button back = new Button("Back to home");
        back.addClickListener(e -> back.getUI().ifPresent(ui -> ui.navigate(FilmRecommendView.class)));

        RouterLink listings = new RouterLink("View all showtimes", FilmListingView.class);
        listings.addClassName("secondary-action");

        Div actions = new Div(back, listings);
        actions.addClassName("film-detail-actions");

        Grid<ShowingRow> showings = new Grid<>(ShowingRow.class, false);
        showings.addColumn(ShowingRow::cinemaName).setHeader("Cinema").setFlexGrow(1);
        showings.addColumn(ShowingRow::screenNumber).setHeader("Screen").setWidth("90px");
        showings.addColumn(row -> row.showDate().format(DATE_FORMAT)).setHeader("Date").setWidth("130px");
        showings.addColumn(row -> row.startTime().format(TIME_FORMAT)).setHeader("Start").setWidth("90px");
        showings.addColumn(ShowingRow::availableSeats).setHeader("Seats").setWidth("90px");
        showings.setItems(film.upcomingShowings());
        showings.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        showings.setWidthFull();
        showings.setAllRowsVisible(true);

        H2 showtimesTitle = new H2("Upcoming showtimes");
        showtimesTitle.addClassName("section-heading");

        Div panel = new Div(showtimesTitle, showings);
        panel.addClassName("surface-panel");
        panel.addClassName("film-detail-showings");

        Div root = new Div(actions, header, panel);
        root.addClassName("film-detail-root");
        return root;
    }
}
