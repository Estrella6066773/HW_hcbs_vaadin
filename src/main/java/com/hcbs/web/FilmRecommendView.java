package com.hcbs.web;

import com.hcbs.dto.FilmCardDto;
import com.hcbs.service.search.HcbsSearchService;
import com.hcbs.web.component.FilmCatalogFilterPanel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
public class FilmRecommendView extends VerticalLayout {
    private final HcbsSearchService searchService;
    private final FilmCatalogFilterPanel filterPanel;
    private final Div grid = new Div();

    public FilmRecommendView(HcbsSearchService searchService) {
        this.searchService = searchService;
        this.filterPanel = new FilmCatalogFilterPanel(this::refresh);

        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        Div hero = pageHero(
                "Home",
                "Browse film posters, search by title, genre, or synopsis, and open a card for details and showtimes."
        );

        grid.addClassName("film-poster-grid");
        grid.setWidthFull();

        hero.setWidthFull();
        add(hero, filterPanel, grid);
        refresh();
    }

    private void refresh() {
        var films = searchService.searchFilms(filterPanel.getFilter());
        grid.removeAll();
        if (films.isEmpty()) {
            Paragraph empty = new Paragraph("No films match your search. Try a different keyword.");
            empty.addClassName("home-empty-message");
            grid.add(empty);
            filterPanel.setResultSummary(0);
        } else {
            films.forEach(film -> grid.add(createCard(film)));
            filterPanel.setResultSummary(films.size());
        }
    }

    private RouterLink createCard(FilmCardDto film) {
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
