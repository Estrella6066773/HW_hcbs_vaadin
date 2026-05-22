package com.hcbs.web;

import com.hcbs.security.AuthUiService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Route target for explicit sign-out (e.g. bookmark or /logout link).
 */
@Route("logout")
@PageTitle("Sign out")
@AnonymousAllowed
public class LogoutView extends Div implements BeforeEnterObserver {

    private final AuthUiService authUiService;

    public LogoutView(AuthUiService authUiService) {
        this.authUiService = authUiService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        authUiService.signOut();
    }
}
