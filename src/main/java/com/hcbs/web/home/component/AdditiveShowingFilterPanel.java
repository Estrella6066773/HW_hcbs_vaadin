package com.hcbs.web.home.component;

import com.hcbs.dto.CinemaOption;
import com.hcbs.dto.CityOption;
import com.hcbs.dto.ShowingListingFilter;
import com.hcbs.service.search.HcbsSearchService;
import com.hcbs.util.EnglishWeekdays;
import com.hcbs.web.home.ShowingFilterQuery;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Additive session search: each filled field narrows results; empty fields are ignored.
 */
public class AdditiveShowingFilterPanel extends Div {
    //继承 Div 表示根节点是一个带 CSS 类名的容器
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final HcbsSearchService searchService;
    private final Runnable onSearch;
    private final Runnable onClear;
    //城市、影院、日期、片名
    private final ComboBox<CityOption> city = new ComboBox<>("City");
    private final ComboBox<CinemaOption> cinema = new ComboBox<>("Cinema");
    private final DatePicker date = new DatePicker("Date");
    private final TextField filmTitle = new TextField("Film title");
    private final Span activeFilters = new Span();//显示当前选了什么；
    private final Span browseSummary = new Span();//显示「共 N 部影片」及是否在筛选模式



    //constructor: 构造函数
    public AdditiveShowingFilterPanel(HcbsSearchService searchService, Runnable onSearch, Runnable onClear) {
        this.searchService = searchService;
        this.onSearch = onSearch;
        this.onClear = onClear;
        ////样式名称
        addClassName("surface-panel");
        addClassName("home-filter-panel");

        //选城市后，影院列表只显示该城市下的影院（listCinemas(cityId)）。
        city.setItems(searchService.listCities());
        city.setClearButtonVisible(true);
        city.setPlaceholder("Any city");
        city.addValueChangeListener(event -> {
            Long cityId = event.getValue() == null ? null : event.getValue().cityId();
            cinema.setItems(searchService.listCinemas(cityId));
             //把影院下拉的可选列表换成当前城市下的影院
            cinema.clear();//换城市就清空已选影院
            refreshActiveFilters();
        });

        //影院、日期、片名
        cinema.setItems(searchService.listCinemas(null));
        cinema.setClearButtonVisible(true);
        cinema.setPlaceholder("Any cinema");
        cinema.addValueChangeListener(event -> refreshActiveFilters());

        EnglishWeekdays.configureDatePicker(date);
        date.setClearButtonVisible(true);
        date.setPlaceholder("Any date");
        date.addValueChangeListener(event -> refreshActiveFilters());
        //刷新当前选了什么
        filmTitle.setPlaceholder("Partial title match");
        filmTitle.setClearButtonVisible(true);
        filmTitle.addValueChangeListener(event -> refreshActiveFilters());
        filmTitle.addKeyPressListener(event -> {
            //按 Enter 等同点 Search 按钮
            if ("Enter".equals(event.getKey())) {
                onSearch.run();
            }
        });

        Button search = new Button("Search", e -> onSearch.run());
        search.addClassName("primary-action");

        Button clear = new Button("Clear", e -> clearFilters());
        clear.addClassName("secondary-action");

        HorizontalLayout filters = new HorizontalLayout(city, cinema, date, filmTitle, search, clear);
        filters.addClassName("filter-bar");
        filters.addClassName("home-filter-bar");
        filters.setDefaultVerticalComponentAlignment(HorizontalLayout.Alignment.END);
        filters.setWidthFull();

        activeFilters.addClassName("home-active-filters");
        browseSummary.addClassName("home-browse-summary");

        VerticalLayout panel = new VerticalLayout(filters, activeFilters, browseSummary);
        panel.setPadding(false);
        panel.setSpacing(true);
        panel.setWidthFull();
        add(panel);
        refreshActiveFilters();
    }

    //获取当前筛选条件，结果显示，把 UI 收成 DTO
    public ShowingListingFilter getFilter() {
        Long cityId = city.getValue() == null ? null : city.getValue().cityId();
        Long cinemaId = cinema.getValue() == null ? null : cinema.getValue().cinemaId();
        return ShowingListingFilter.of(cityId, cinemaId, date.getValue(), filmTitle.getValue());
    }

    public void setBrowseSummary(int filmCount, boolean filtered) {
        browseSummary.setVisible(true);
        activeFilters.setVisible(true);
        String scope = filtered ? " matching your search" : " on display";
        browseSummary.setText(filmCount + (filmCount == 1 ? " film" : " films") + scope);
    }

    //清空并通知父页面
    public void clearFilters() {
        city.clear();
        cinema.setItems(searchService.listCinemas(null));
        cinema.clear();
        date.clear();
        filmTitle.clear();
        refreshActiveFilters();
        onClear.run();
    }

    private void refreshActiveFilters() {
        List<String> parts = new ArrayList<>();
        if (city.getValue() != null) {
            parts.add("City: " + city.getValue().name());
        }
        if (cinema.getValue() != null) {
            parts.add("Cinema: " + cinema.getValue().name());
        }
        if (date.getValue() != null) {
            parts.add("Date: " + date.getValue().format(DATE_FORMAT));
        }
        if (filmTitle.getValue() != null && !filmTitle.getValue().isBlank()) {
            parts.add("Title: \"" + filmTitle.getValue().trim() + "\"");
        }
        if (parts.isEmpty()) {
            activeFilters.setText("Browse all films below, or add conditions and click Search to see which films you can watch.");
        } else {
            activeFilters.setText("Active filters: " + String.join(" · ", parts));
        }
    }
//从 URL / DTO 回填面板
    public void applyFilter(ShowingListingFilter filter) {
        if (filter == null) {
            return;
        }
        if (filter.cityId() != null) {
            city.setValue(searchService.listCities().stream()
                    .filter(option -> option.cityId().equals(filter.cityId()))
                    .findFirst()
                    .orElse(null));
            cinema.setItems(searchService.listCinemas(filter.cityId()));
        } else {
            city.clear();
            cinema.setItems(searchService.listCinemas(null));
        }
        if (filter.cinemaId() != null) {
            cinema.setValue(searchService.listCinemas(filter.cityId()).stream()
                    .filter(option -> option.cinemaId().equals(filter.cinemaId()))
                    .findFirst()
                    .orElse(null));
        } else {
            cinema.clear();
        }
        date.setValue(filter.date());
        filmTitle.setValue(filter.filmTitle() == null ? "" : filter.filmTitle());
        refreshActiveFilters();
    }

    public boolean hasActiveCriteria() {
        return ShowingFilterQuery.hasCriteria(getFilter());
    }
}
