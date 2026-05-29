package com.hcbs.web;

import com.hcbs.dto.FilmCardDto;
import com.hcbs.dto.FilmCatalogFilter;
import com.hcbs.dto.ShowingListingFilter;
import com.hcbs.service.catalog.PosterResourceService;
import com.hcbs.service.search.HcbsSearchService;
import com.hcbs.web.component.AdditiveShowingFilterPanel;
import com.hcbs.web.component.FilmPoster;
import com.hcbs.web.component.PageHero;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
@AnonymousAllowed
public class FilmRecommendView extends VerticalLayout implements BeforeEnterObserver {

    private final HcbsSearchService searchService;
    private final PosterResourceService posterResources;
    private final AdditiveShowingFilterPanel filterPanel;
    private final Div posterGrid = new Div();
    private boolean filteredBrowse;

    public FilmRecommendView(HcbsSearchService searchService, PosterResourceService posterResources) {
        this.searchService = searchService;
        this.posterResources = posterResources;
        this.filterPanel = new AdditiveShowingFilterPanel(searchService, this::runSearch, this::showAllFilms);

        setWidthFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        PageHero hero = new PageHero(
                "Films",
                "Home",
                "Browse films on display, or search by city, cinema, date, and title to see what you can watch."
        );

        filterPanel.setWidthFull();

        posterGrid.addClassName("film-poster-grid");
        posterGrid.setWidthFull();

        add(hero, filterPanel, posterGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        ShowingListingFilter fromUrl = ShowingFilterQuery.fromQueryParameters(event.getLocation().getQueryParameters());
        if (ShowingFilterQuery.hasCriteria(fromUrl)) {
            filterPanel.applyFilter(fromUrl);
            filteredBrowse = true;
            displayFilteredFilms(fromUrl);
        } else {
            filteredBrowse = false;
            displayAllFilms();
        }
    }

    private void runSearch() {
        getUI().ifPresent(ui -> ui.navigate(
                FilmRecommendView.class,
                ShowingFilterQuery.toQueryParameters(filterPanel.getFilter())));
    }

    private void showAllFilms() {
        getUI().ifPresent(ui -> ui.navigate(FilmRecommendView.class, QueryParameters.empty()));
    }

    private void displayAllFilms() {
        List<FilmCardDto> films = searchService.searchFilms(FilmCatalogFilter.of(""));
        renderPosters(films);
        filterPanel.setBrowseSummary(films.size(), false);
    }

    private void displayFilteredFilms(ShowingListingFilter filter) {
        List<FilmCardDto> films = searchService.searchFilmsByShowings(filter);
        renderPosters(films);
        filterPanel.setBrowseSummary(films.size(), true);
    }

    private void renderPosters(List<FilmCardDto> films) {
        posterGrid.removeAll();
        films.forEach(film -> posterGrid.add(createPosterCard(film)));
    }

    private RouterLink createPosterCard(FilmCardDto film) {
        FilmPoster poster = new FilmPoster(posterResources, film.posterUrl(), film.title() + " poster");

        H2 title = new H2(film.title());
        title.addClassName("film-poster-title");

        Span meta = new Span(film.genre() + " · " + film.ageRating() + " · ★ " + film.rating());
        meta.addClassName("film-poster-meta");

        RouterLink link = new RouterLink();
        link.setRoute(FilmDetailView.class, film.filmId());
        if (filteredBrowse) {
            link.setQueryParameters(ShowingFilterQuery.toQueryParameters(filterPanel.getFilter()));
        }
        link.addClassName("film-poster-card");
        link.add(poster, title, meta);
        return link;
    }
}
