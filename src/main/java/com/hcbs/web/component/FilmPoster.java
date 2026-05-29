package com.hcbs.web.component;

import com.hcbs.service.catalog.PosterResourceService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

/**
 * Renders a film poster from a DB path, or a missing placeholder when the file is absent.
 */
public class FilmPoster extends Div {

    public FilmPoster(PosterResourceService posterResources, String posterUrl, String altText, String... classNames) {
        addClassName("film-poster-slot");
        for (String className : classNames) {
            addClassName(className);
        }

        if (posterResources.isAvailable(posterUrl)) {
            Image image = new Image(posterUrl.trim(), altText);
            image.addClassName("film-poster-image");
            add(image);
        } else {
            Div missing = new Div();
            missing.addClassName("film-poster-missing");
            missing.add(new Span("缺失"));
            add(missing);
        }
    }
}
