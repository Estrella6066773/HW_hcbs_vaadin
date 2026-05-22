package com.hcbs.web.component;

import com.hcbs.dto.ShowingListingFilter;
import com.hcbs.web.FilmRecommendView;
import com.hcbs.web.ShowingFilterQuery;
import com.vaadin.flow.component.button.Button;
/**
 * Navigate back to the home view, optionally preserving session search filters.
 */
public class BackToHomeAction extends Button {

    public BackToHomeAction() {
        this(null);
    }

    public BackToHomeAction(ShowingListingFilter filter) {
        super("返回主页");
        addClickListener(event -> getUI().ifPresent(ui -> {
            if (filter != null && ShowingFilterQuery.hasCriteria(filter)) {
                ui.navigate(FilmRecommendView.class, ShowingFilterQuery.toQueryParameters(filter));
            } else {
                ui.navigate(FilmRecommendView.class);
            }
        }));
    }
}
