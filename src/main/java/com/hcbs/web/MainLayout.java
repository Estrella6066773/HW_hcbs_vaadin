package com.hcbs.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {
    public MainLayout() {
        addClassName("hcbs-shell");

        Div mark = new Div("HC");
        mark.addClassName("brand-mark");

        H1 title = new H1("Horizon Cinemas");
        Span subtitle = new Span("Booking System Control Desk");
        subtitle.addClassName("brand-subtitle");

        VerticalLayout brandText = new VerticalLayout(title, subtitle);
        brandText.addClassName("brand-text");
        brandText.setPadding(false);
        brandText.setSpacing(false);

        HorizontalLayout brand = new HorizontalLayout(mark, brandText);
        brand.addClassName("brand-lockup");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), brand);
        header.addClassName("topbar");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();

        addToNavbar(header);

        Span drawerLabel = new Span("Operations");
        drawerLabel.addClassName("drawer-label");
        addToDrawer(drawerLabel);
        addToDrawer(navLink("Home", FilmRecommendView.class, "Browse and search films"));
        addToDrawer(navLink("Film Listing", FilmListingView.class, "Now showing"));
        addToDrawer(navLink("Booking", BookingView.class, "Ticket desk"));
        addToDrawer(navLink("Cancellation", CancellationView.class, "Refund desk"));
    }

    private RouterLink navLink(String title, Class<? extends Component> route, String caption) {
        RouterLink link = new RouterLink();
        link.setRoute(route);
        link.addClassName("nav-card");

        Span name = new Span(title);
        name.addClassName("nav-title");
        Span detail = new Span(caption);
        detail.addClassName("nav-caption");
        link.add(name, detail);
        return link;
    }
}
