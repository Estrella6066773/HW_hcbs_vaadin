package com.hcbs.web;

import com.hcbs.dto.FilmCardDto;
import com.hcbs.dto.FilmCatalogFilter;
import com.hcbs.dto.ShowingListingFilter;
import com.hcbs.service.search.HcbsSearchService;
import com.hcbs.web.component.AdditiveShowingFilterPanel;
import com.hcbs.web.component.PageHero;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
@AnonymousAllowed
public class FilmRecommendView extends VerticalLayout implements BeforeEnterObserver {

    private final HcbsSearchService searchService;
    private final AdditiveShowingFilterPanel filterPanel;
    private final Div featureCarousel = new Div();
    private final Div posterGrid = new Div();
    private boolean filteredBrowse;

    public FilmRecommendView(HcbsSearchService searchService) {
        this.searchService = searchService;
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

        featureCarousel.addClassName("home-feature-carousel");
        featureCarousel.setWidthFull();

        filterPanel.setWidthFull();

        posterGrid.addClassName("film-poster-grid");
        posterGrid.setWidthFull();

        add(hero, featureCarousel, filterPanel, posterGrid);
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
        renderFeatureCarousel(films);
        renderPosters(films);
        filterPanel.setBrowseSummary(films.size(), false);
    }

    private void displayFilteredFilms(ShowingListingFilter filter) {
        List<FilmCardDto> films = searchService.searchFilmsByShowings(filter);
        renderFeatureCarousel(searchService.searchFilms(FilmCatalogFilter.of("")));
        renderPosters(films);
        filterPanel.setBrowseSummary(films.size(), true);
    }

    private void renderFeatureCarousel(List<FilmCardDto> films) {
        featureCarousel.removeAll();

        Map<String, FilmCardDto> filmsByTitle = films.stream()
                .collect(Collectors.toMap(FilmCardDto::title, Function.identity(), (first, ignored) -> first));
        List<FeatureBanner> banners = List.of(
                new FeatureBanner("Spirited Away", "/images/banners/spirited-away-banner.png", false),
                new FeatureBanner("The Legend of 1900", "/images/banners/legend-of-1900-banner.png", false),
                new FeatureBanner("Call Me by Your Name", "/images/banners/call-me-by-your-name-banner.jpg", false),
                new FeatureBanner("Maleficent", "/images/banners/maleficent-banner.png", true)
        );

        List<FeaturedFilm> slides = banners.stream()
                .map(banner -> Optional.ofNullable(filmsByTitle.get(banner.title()))
                        .map(film -> new FeaturedFilm(film, banner.imageUrl(), banner.contained())))
                .flatMap(Optional::stream)
                .toList();

        if (slides.isEmpty()) {
            featureCarousel.setVisible(false);
            return;
        }

        featureCarousel.setVisible(true);
        Div viewport = new Div();
        viewport.addClassName("home-feature-viewport");

        Div track = new Div();
        track.addClassName("home-feature-track");

        slides.forEach(slide -> track.add(createFeatureSlide(slide)));

        Button previous = new Button("‹");
        previous.addClassName("home-feature-arrow");
        previous.addClassName("home-feature-arrow-prev");
        previous.getElement().setAttribute("aria-label", "Previous featured film");
        previous.getElement().setAttribute("data-feature-prev", "true");

        Button next = new Button("›");
        next.addClassName("home-feature-arrow");
        next.addClassName("home-feature-arrow-next");
        next.getElement().setAttribute("aria-label", "Next featured film");
        next.getElement().setAttribute("data-feature-next", "true");

        viewport.add(track, previous, next);
        featureCarousel.add(viewport, createFeatureControls(slides));
        enableFeatureCarousel(slides.size());
    }

    private RouterLink createFeatureSlide(FeaturedFilm slide) {
        FilmCardDto film = slide.film();
        Image banner = new Image(slide.imageUrl(), film.title() + " banner");
        banner.addClassName("home-feature-image");
        if (slide.contained()) {
            banner.addClassName("home-feature-image-contained");
        }

        Span label = new Span("Featured Film");
        label.addClassName("home-feature-label");
        H2 title = new H2(film.title());
        title.addClassName("home-feature-title");
        Span meta = new Span(film.genre() + " / " + film.ageRating() + " / Rating " + film.rating());
        meta.addClassName("home-feature-meta");
        Span detailButton = new Span("View details");
        detailButton.addClassName("home-feature-cta");

        Div copy = new Div(label, title, meta, detailButton);
        copy.addClassName("home-feature-copy");

        RouterLink link = new RouterLink();
        link.setRoute(FilmDetailView.class, film.filmId());
        link.addClassName("home-feature-slide");
        link.add(banner, copy);
        return link;
    }

    private Div createFeatureControls(List<FeaturedFilm> slides) {
        Div controls = new Div();
        controls.addClassName("home-feature-controls");

        Checkbox autoScroll = new Checkbox("Auto scroll");
        autoScroll.setValue(true);
        autoScroll.addClassName("home-feature-auto");
        autoScroll.getElement().setAttribute("data-auto-scroll", "true");

        controls.add(autoScroll);
        return controls;
    }

    private void enableFeatureCarousel(int slideCount) {
        featureCarousel.getElement().executeJs("""
                const root = this;
                const track = root.querySelector('.home-feature-track');
                const previous = root.querySelector('[data-feature-prev]');
                const next = root.querySelector('[data-feature-next]');
                const auto = root.querySelector('[data-auto-scroll]');
                if (!track || !previous || !next || !auto) {
                    return;
                }
                if (root.__homeFeatureTimer) {
                    window.clearInterval(root.__homeFeatureTimer);
                }
                let current = 0;
                const total = $0;
                const show = (next) => {
                    current = (next + total) % total;
                    track.style.transform = `translateX(${-current * 100}%)`;
                };
                const start = () => {
                    window.clearInterval(root.__homeFeatureTimer);
                    root.__homeFeatureTimer = window.setInterval(() => show(current + 1), 4200);
                };
                const manualMove = (step) => {
                    show(current + step);
                    if (auto.checked) {
                        start();
                    }
                };
                previous.addEventListener('click', () => manualMove(-1));
                next.addEventListener('click', () => manualMove(1));
                auto.addEventListener('change', () => {
                    if (auto.checked) {
                        start();
                    } else {
                        window.clearInterval(root.__homeFeatureTimer);
                    }
                });
                show(0);
                if (auto.checked) {
                    start();
                }
                """, slideCount);
    }

    private void renderPosters(List<FilmCardDto> films) {
        posterGrid.removeAll();
        films.forEach(film -> posterGrid.add(createPosterCard(film)));
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
        if (filteredBrowse) {
            link.setQueryParameters(ShowingFilterQuery.toQueryParameters(filterPanel.getFilter()));
        }
        link.addClassName("film-poster-card");
        link.add(poster, title, meta);
        return link;
    }

    private record FeatureBanner(String title, String imageUrl, boolean contained) {
    }

    private record FeaturedFilm(FilmCardDto film, String imageUrl, boolean contained) {
    }
}
