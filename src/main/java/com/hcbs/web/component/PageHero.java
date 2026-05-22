package com.hcbs.web.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

/**
 * Page header banner shared across main views (home, booking, cancellation, etc.).
 */
public class PageHero extends Div {

    public PageHero(String eyebrow, String heading, String copy) {
        Span badge = new Span(eyebrow);
        badge.addClassName("eyebrow");

        H2 title = new H2(heading);
        title.addClassName("page-hero-heading");

        Paragraph description = new Paragraph(copy);
        description.addClassName("page-hero-copy");

        add(badge, title, description);
        addClassName("page-hero");
        setWidthFull();
        getElement().getStyle().set("flex-grow", "0").set("flex-shrink", "0");
    }
}
