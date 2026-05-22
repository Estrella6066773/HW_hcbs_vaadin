package com.hcbs.web.component;

import com.hcbs.dto.CinemaOption;
import com.hcbs.dto.CityOption;
import com.hcbs.dto.ShowingListingFilter;
import com.hcbs.service.search.HcbsSearchService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;

/**
 * Film Listing filter bar: city, cinema, date, and film title.
 */
public class ShowingListingFilterPanel extends Div {

    private final HcbsSearchService searchService;
    private final ComboBox<CityOption> city = new ComboBox<>("City");
    private final ComboBox<CinemaOption> cinema = new ComboBox<>("Cinema");
    private final DatePicker date = new DatePicker("Date");
    private final TextField filmTitle = new TextField("Film title");
    private final Runnable onSearch;

    public ShowingListingFilterPanel(HcbsSearchService searchService, Runnable onSearch) {
        this.searchService = searchService;
        this.onSearch = onSearch;
        addClassName("surface-panel");

        city.setItems(searchService.listCities());
        city.addValueChangeListener(event -> {
            Long cityId = event.getValue() == null ? null : event.getValue().cityId();
            cinema.setItems(searchService.listCinemas(cityId));
            cinema.clear();
        });
        cinema.setItems(searchService.listCinemas(null));
        date.setValue(LocalDate.now().plusDays(3));

        filmTitle.setPlaceholder("Partial title match");
        filmTitle.setClearButtonVisible(true);
        filmTitle.addKeyPressListener(event -> {
            if ("Enter".equals(event.getKey())) {
                onSearch.run();
            }
        });

        Button search = new Button("Search", e -> onSearch.run());
        search.addClassName("primary-action");

        HorizontalLayout filters = new HorizontalLayout(city, cinema, date, filmTitle, search);
        filters.addClassName("filter-bar");
        filters.setDefaultVerticalComponentAlignment(HorizontalLayout.Alignment.END);
        add(filters);
    }

    public ShowingListingFilter getFilter() {
        Long cityId = city.getValue() == null ? null : city.getValue().cityId();
        Long cinemaId = cinema.getValue() == null ? null : cinema.getValue().cinemaId();
        return ShowingListingFilter.of(cityId, cinemaId, date.getValue(), filmTitle.getValue());
    }
}
