package com.hcbs.web.component;

import com.hcbs.dto.FilmCatalogFilter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Home page filter bar: keyword search for the film catalog.
 */
public class FilmCatalogFilterPanel extends Div {

    private final TextField keyword = new TextField("Search films");
    private final Span resultCount = new Span();
    private final Runnable onSearch;

    public FilmCatalogFilterPanel(Runnable onSearch) {
        this.onSearch = onSearch;
        addClassName("surface-panel");

        keyword.setPlaceholder("Title, genre, or synopsis");
        keyword.setClearButtonVisible(true);
        keyword.setWidth("320px");
        keyword.addKeyPressListener(event -> {
            if ("Enter".equals(event.getKey())) {
                onSearch.run();
            }
        });
        keyword.addValueChangeListener(event -> {
            if (event.getValue() == null || event.getValue().isBlank()) {
                onSearch.run();
            }
        });

        Button search = new Button("Search", e -> onSearch.run());
        search.addClassName("primary-action");

        resultCount.addClassName("home-result-count");

        HorizontalLayout filters = new HorizontalLayout(keyword, search, resultCount);
        filters.addClassName("filter-bar");
        filters.setDefaultVerticalComponentAlignment(HorizontalLayout.Alignment.END);
        add(filters);
    }

    public FilmCatalogFilter getFilter() {
        return FilmCatalogFilter.of(keyword.getValue());
    }

    public void setResultSummary(int count) {
        resultCount.setText(count + (count == 1 ? " film" : " films"));
    }
}
