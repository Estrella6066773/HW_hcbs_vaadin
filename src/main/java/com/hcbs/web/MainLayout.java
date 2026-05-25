package com.hcbs.web;

import com.hcbs.model.User;
import com.hcbs.security.AuthUiService;
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
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final CurrentUserService currentUserService;
    private final AuthUiService authUiService;
    private final Span subtitle = new Span("Cinema booking portal");

    private final HorizontalLayout headerActions = new HorizontalLayout();
    private final VerticalLayout drawerContent = new VerticalLayout();

    public MainLayout(CurrentUserService currentUserService, AuthUiService authUiService) {
        this.currentUserService = currentUserService;
        this.authUiService = authUiService;
        addClassName("hcbs-shell");

        Div mark = new Div("HC");
        mark.addClassName("brand-mark");

        H1 title = new H1("Horizon Cinemas");
        subtitle.addClassName("brand-subtitle");

        VerticalLayout brandText = new VerticalLayout(title, subtitle);
        brandText.addClassName("brand-text");
        brandText.setPadding(false);
        brandText.setSpacing(false);

        HorizontalLayout brand = new HorizontalLayout(mark, brandText);
        brand.addClassName("brand-lockup");

        headerActions.addClassName("header-actions");
        headerActions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerActions.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), brand, headerActions);
        header.addClassName("topbar");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.expand(brand);

        addToNavbar(header);

        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.setWidthFull();
        addToDrawer(drawerContent);

        refreshChrome();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        refreshChrome();
    }

    private void refreshChrome() {
        refreshHeader();
        refreshDrawer();
        subtitle.setText(resolveSubtitle());
    }

    private void refreshHeader() {
        headerActions.removeAll();
        Button account = new Button("个人中心", event -> openAccountCenter());
        account.addClassName("primary-action");
        account.addClassName("account-button");

        if (currentUserService.isAuthenticated()) {
            User user = currentUserService.requireCurrentUser();
            Span userBadge = new Span(user.getFullName());
            userBadge.addClassName("user-badge");

            Button signOut = new Button("Sign out", event -> authUiService.signOut());
            signOut.addClassName("secondary-action");
            signOut.addClassName("sign-out-button");

            headerActions.add(userBadge, account, signOut);
        } else {
            headerActions.add(account);
        }
    }

    private void refreshDrawer() {
        drawerContent.removeAll();

        Span drawerLabel = new Span("Menu");
        drawerLabel.addClassName("drawer-label");
        drawerContent.add(drawerLabel);
        drawerContent.add(navLink("Home", FilmRecommendView.class, "Browse films and find showtimes"));
        if (currentUserService.isAuthenticated() && currentUserService.requireCurrentUser().getRole().isEmployee()) {
            drawerContent.add(navLink("Book tickets", BookingView.class, "Desk booking for any showing"));
        }

        if (currentUserService.isAuthenticated()) {
            User user = currentUserService.requireCurrentUser();
            if (user.getRole().isCustomer()) {
                drawerContent.add(navLink("My bookings", MyBookingsView.class, "View and cancel your orders"));
            } else {
                drawerContent.add(navLink("Cancellation", CancellationView.class, "Refund desk for any booking"));
                if (user.getRole().canAccessAdminTools()) {
                    drawerContent.add(navLink("Data admin", AdminDataView.class, "Manage films and accounts"));
                }
            }
        }
    }

    private String resolveSubtitle() {
        return currentUserService.findCurrentUser()
                .map(user -> user.getRole().isCustomer()
                        ? "Customer self-service"
                        : "Employee control desk")
                .orElse("Cinema booking portal");
    }

    private void openAccountCenter() {
        getUI().ifPresent(ui -> {
            if (currentUserService.isAuthenticated()) {
                ui.navigate(AccountCenterView.class);
            } else {
                ui.navigate(LoginView.class);
            }
        });
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
