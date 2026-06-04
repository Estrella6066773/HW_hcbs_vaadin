package com.hcbs.web.home;

import com.hcbs.dto.FilmCardDto;
import com.hcbs.dto.FilmCatalogFilter;
import com.hcbs.dto.ShowingListingFilter;
import com.hcbs.service.catalog.PosterResourceService;
import com.hcbs.service.search.HcbsSearchService;
import com.hcbs.web.component.PageHero;
import com.hcbs.web.home.component.AdditiveShowingFilterPanel;
import com.hcbs.web.home.component.FilmPoster;
import com.hcbs.web.shell.MainLayout;
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

@Route(value = "", layout = MainLayout.class)//
@PageTitle("Home")//浏览器标签标题
@AnonymousAllowed//未登录也能浏览
public class FilmRecommendView extends VerticalLayout implements BeforeEnterObserver {
    //BeforeEnterObserver: 在进入页面之前执行，extends VerticalLayout = 页面根布局是 Vaadin 组件。



    private final HcbsSearchService searchService;//搜索服务
    private final PosterResourceService posterResources;//海报资源服务
    private final AdditiveShowingFilterPanel filterPanel;//筛选面板，城市、影院、日期、片名等筛选 UI
    //vaadin组件：Div，用于容器布局
    private final Div featureCarousel = new Div();
    private final Div posterGrid = new Div();
    private boolean filteredBrowse;//是否筛选
//private final = 仅本类使用，且初始化后不再换绑定的字段。

    public FilmRecommendView(HcbsSearchService searchService, PosterResourceService posterResources) {
        this.searchService = searchService;//搜索服务：HcbsSearchService
        this.posterResources = posterResources;//海报资源服务：PosterResourceService
        this.filterPanel = new AdditiveShowingFilterPanel(searchService, this::runSearch, this::showAllFilms);
        //FilmRecommendView 创建 AdditiveShowingFilterPanel 实例；
        

        setWidthFull();//设置宽度为全宽
        setPadding(false);//设置内边距为0
        setMargin(false);//设置外边距为0
        addClassName("page-view");
//"page-view" 类名：在 styles.css 里定义的样式，用于设置页面布局和样式。
        PageHero hero = new PageHero(
                "Films",
                "Home",
                "Browse films on display, or search by city, cinema, date, and title to see what you can watch."
        );
        //PageHero: 页头标题区（eyebrow + 标题 + 描述）

        featureCarousel.addClassName("home-feature-carousel");
        featureCarousel.setWidthFull();

        filterPanel.setWidthFull();

        posterGrid.addClassName("film-poster-grid");
        posterGrid.setWidthFull();//填充的是最下面的 posterGrid，电影卡片列表展示

        add(hero, featureCarousel, filterPanel, posterGrid);
    }//给轮播/筛选/海报容器加 CSS 类、全宽，并 add 到页面

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        ShowingListingFilter fromUrl = ShowingFilterQuery.fromQueryParameters(event.getLocation().getQueryParameters());
       //URL query → ShowingListingFilter → 在选项列表里按 id 找 CityOption/CinemaOption → 设到控件。
        if (ShowingFilterQuery.hasCriteria(fromUrl)) {
            filterPanel.applyFilter(fromUrl);
            filteredBrowse = true;
            displayFilteredFilms(fromUrl);
        } else {
            filteredBrowse = false;//如果 URL 没有筛选条件，则设置 filteredBrowse 为 false
            displayAllFilms();
        }
    }

    private void runSearch() {
        //把面板当前条件编成 QueryParameters，导航到本页（URL 变、触发新的 beforeEnter）。这样链接可分享、可收藏。
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
    
    //精选轮播：按场次筛选
    private void displayFilteredFilms(ShowingListingFilter filter) {
        // 页面（Vaadin）只调 Service；Service 再调数据库层 — Spring Boot 把这条链串好。
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

        //写轮播标题、副标题、评分、详情按钮
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
        //只带影片 ID，不会像下方海报卡片那样附带筛选用的 query 参数
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

    //单张「电影卡片」：海报 + 标题 + 副标题
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

    //是轮播配置用的 record
    private record FeatureBanner(String title, String imageUrl, boolean contained) {
    }

    private record FeaturedFilm(FilmCardDto film, String imageUrl, boolean contained) {
    }
}
