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

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final HcbsSearchService searchService;
    private final Runnable onSearch;
    private final Runnable onClear;
    private final ComboBox<CityOption> city = new ComboBox<>("City");
    private final ComboBox<CinemaOption> cinema = new ComboBox<>("Cinema");
    private final DatePicker date = new DatePicker("Date");
    private final TextField filmTitle = new TextField("Film title");
    private final Span activeFilters = new Span();
    private final Span browseSummary = new Span();

    public AdditiveShowingFilterPanel(HcbsSearchService searchService, Runnable onSearch, Runnable onClear) {
        this.searchService = searchService;
        this.onSearch = onSearch;
        this.onClear = onClear;
        addClassName("surface-panel");
        addClassName("home-filter-panel");

        city.setItems(searchService.listCities());
        city.setClearButtonVisible(true);
        city.setPlaceholder("Any city");
        city.addValueChangeListener(event -> {
            Long cityId = event.getValue() == null ? null : event.getValue().cityId();
            cinema.setItems(searchService.listCinemas(cityId));
            cinema.clear();
            refreshActiveFilters();
        });

        cinema.setItems(searchService.listCinemas(null));
        cinema.setClearButtonVisible(true);
        cinema.setPlaceholder("Any cinema");
        cinema.addValueChangeListener(event -> refreshActiveFilters());

        EnglishWeekdays.configureDatePicker(date);
        date.setClearButtonVisible(true);
        date.setPlaceholder("Any date");
        date.addValueChangeListener(event -> refreshActiveFilters());

        filmTitle.setPlaceholder("Partial title match");
        filmTitle.setClearButtonVisible(true);
        filmTitle.addValueChangeListener(event -> refreshActiveFilters());
        filmTitle.addKeyPressListener(event -> {
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
