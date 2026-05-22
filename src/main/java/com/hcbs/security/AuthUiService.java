package com.hcbs.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.stereotype.Service;

/**
 * Centralizes sign-in UI actions (logout) for Vaadin views.
 */
@Service
public class AuthUiService {

    private final AuthenticationContext authenticationContext;

    public AuthUiService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    /**
     * Signs out the current user and returns to the public home page.
     */
    public void signOut() {
        authenticationContext.logout();
    }
}
