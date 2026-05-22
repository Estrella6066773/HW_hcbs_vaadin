package com.hcbs.web;

import com.hcbs.dto.RegistrationRequest;
import com.hcbs.service.auth.RegistrationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Create account")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final RegistrationService registrationService;

    private final TextField username = new TextField("Username");
    private final EmailField email = new EmailField("Email");
    private final TextField fullName = new TextField("Full name");
    private final TextField phone = new TextField("Phone (optional)");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm password");

    public RegisterView(RegistrationService registrationService) {
        this.registrationService = registrationService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-view");

        username.setHelperText("3–32 characters: letters, numbers, . _ -");
        username.setMaxLength(32);
        email.setClearButtonVisible(true);
        fullName.setMaxLength(100);
        phone.setHelperText("Optional contact number");
        password.setHelperText("At least 8 characters");
        password.setRevealButtonVisible(true);
        confirmPassword.setRevealButtonVisible(true);

        FormLayout form = new FormLayout(username, email, fullName, phone, password, confirmPassword);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setWidthFull();

        Button submit = new Button("Create account", event -> submit());
        submit.addClassName("primary-action");

        Anchor signIn = new Anchor("login", "Already have an account? Sign in");
        signIn.addClassName("register-link");

        Span note = new Span("Customer accounts only. Staff accounts are created by cinema administrators.");
        note.addClassName("login-hint");

        VerticalLayout card = new VerticalLayout(
                new H1("Join Horizon Cinemas"),
                new Paragraph("Register to book tickets and manage your orders online."),
                note,
                form,
                submit,
                signIn
        );
        card.addClassName("login-card");
        card.setAlignItems(Alignment.STRETCH);
        card.setWidth("480px");
        add(card);
    }

    private void submit() {
        RegistrationRequest request = new RegistrationRequest(
                username.getValue(),
                email.getValue(),
                password.getValue(),
                confirmPassword.getValue(),
                fullName.getValue(),
                phone.getValue()
        );
        try {
            registrationService.registerCustomer(request);
            getUI().ifPresent(ui -> ui.navigate("login?registered"));
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }
}
