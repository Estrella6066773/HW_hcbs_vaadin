package com.hcbs.web;

import com.hcbs.model.User;
import com.hcbs.model.UserRole;
import com.hcbs.security.CurrentUserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final CurrentUserService currentUserService;

    public MainLayout(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
        addClassName("hcbs-shell");

        Div mark = new Div("HC");
        mark.addClassName("brand-mark");

        H1 title = new H1("Horizon Cinemas");
        Span subtitle = new Span(resolveSubtitle());
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
        header.expand(brand);

        try {
            User user = currentUserService.requireCurrentUser();
            Span userBadge = new Span(user.getFullName() + " · " + formatRole(user.getRole()));
            userBadge.addClassName("user-badge");
            Button signOut = new Button("Sign out", event ->
                    getUI().ifPresent(ui -> ui.getPage().setLocation("logout")));
            signOut.addClassName("secondary-action");
            header.add(userBadge, signOut);
        } catch (RuntimeException ignored) {
            Button register = new Button("Register", event ->
                    getUI().ifPresent(ui -> ui.navigate(RegisterView.class)));
            register.addClassName("secondary-action");
            Button signIn = new Button("Sign in", event ->
                    getUI().ifPresent(ui -> ui.navigate(LoginView.class)));
            signIn.addClassName("secondary-action");
            header.add(register, signIn);
        }

        addToNavbar(header);

        Span drawerLabel = new Span("Menu");
        drawerLabel.addClassName("drawer-label");
        addToDrawer(drawerLabel);
        addToDrawer(navLink("Home", FilmRecommendView.class, "Browse and search showtimes"));

        try {
            User user = currentUserService.requireCurrentUser();
            if (user.getRole().isCustomer()) {
                addToDrawer(navLink("Book tickets", BookingView.class, "Book seats for yourself"));
                addToDrawer(navLink("My bookings", MyBookingsView.class, "View and cancel your orders"));
            } else {
                addToDrawer(navLink("Ticket desk", BookingView.class, "Book on behalf of a customer"));
                addToDrawer(navLink("Cancellation", CancellationView.class, "Refund desk for any booking"));
                addToDrawer(navLink("Data admin", AdminDataView.class, "Manage films and accounts"));
            }
        } catch (RuntimeException ignored) {
            // Signed-out users only see public home in the drawer.
        }
    }

    private String resolveSubtitle() {
        try {
            User user = currentUserService.requireCurrentUser();
            return user.getRole().isCustomer()
                    ? "Customer self-service"
                    : "Employee control desk";
        } catch (RuntimeException ex) {
            return "Cinema booking portal";
        }
    }

    private static String formatRole(UserRole role) {
        return switch (role) {
            case CUSTOMER -> "Customer";
            case BOOKING_STAFF -> "Booking staff";
            case ADMIN -> "Admin";
            case MANAGER -> "Manager";
        };
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
