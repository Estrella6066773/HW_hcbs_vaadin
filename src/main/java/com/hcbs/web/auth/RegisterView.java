package com.hcbs.web.auth;

import com.hcbs.dto.RegistrationRequest;
import com.hcbs.service.auth.RegistrationService;
import com.hcbs.web.home.component.BackToHomeAction;
import com.hcbs.web.component.PageHero;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * 客户注册页（成员 C · 账户模块）。
 * <p>
 * 路由 {@code /register}，全屏独立页。仅允许注册 {@link com.hcbs.model.UserRole#CUSTOMER}，
 * 员工账号由管理员在后台创建。表单提交委托 {@link com.hcbs.service.auth.RegistrationService}，
 * 成功后跳转 {@code /login?registered}。
 */
@Route("register")
@PageTitle("Create account")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final RegistrationService registrationService;

    private final TextField username = new TextField("Username");
    private final TextField phone = new TextField("Phone number");
    private final EmailField email = new EmailField("Email (optional)");
    private final TextField fullName = new TextField("Full name");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm password");

    public RegisterView(RegistrationService registrationService) {
        this.registrationService = registrationService;
        setWidthFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");
        addClassName("auth-page");

        Div actions = new Div(new BackToHomeAction());
        actions.addClassName("film-detail-actions");

        PageHero hero = new PageHero(
                "Customer",
                "Create account",
                "Register to book tickets and manage your orders online."
        );

        Paragraph note = new Paragraph(
                "Customer accounts only. Staff accounts are created by cinema administrators.");
        note.addClassName("auth-hint");

        username.setHelperText("3–32 characters: letters, numbers, . _ -");
        username.setMaxLength(32);
        phone.setHelperText("Required for sign in and booking lookup");
        phone.setRequiredIndicatorVisible(true);
        phone.getElement().setAttribute("autocomplete", "tel");
        email.setClearButtonVisible(true);
        email.getElement().setAttribute("autocomplete", "email");
        fullName.setMaxLength(100);
        password.setHelperText("At least 8 characters");
        password.setRevealButtonVisible(true);
        confirmPassword.setRevealButtonVisible(true);

        FormLayout form = new FormLayout(username, phone, fullName, email, password, confirmPassword);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setWidthFull();

        Button submit = new Button("Create account", event -> submit());
        submit.addClassName("primary-action");

        Anchor signIn = new Anchor("login", "Already have an account? Sign in");
        signIn.addClassName("auth-link");

        Div formPanel = new Div(note, form, submit);
        formPanel.addClassName("auth-panel");

        VerticalLayout page = new VerticalLayout(actions, hero, formPanel, signIn);
        page.addClassName("auth-page-content");
        page.setPadding(false);
        page.setSpacing(true);
        page.setWidthFull();

        add(page);
    }

    /** 组装 {@link com.hcbs.dto.RegistrationRequest} 并调用注册服务；校验失败用 Notification 提示 */
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
