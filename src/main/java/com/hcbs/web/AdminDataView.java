package com.hcbs.web;

import com.hcbs.dto.FilmAdminRow;
import com.hcbs.model.User;
import com.hcbs.model.UserStatus;
import com.hcbs.service.admin.AdminCatalogService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Data admin")
@RolesAllowed({"ADMIN", "MANAGER"})
public class AdminDataView extends VerticalLayout {

    private final AdminCatalogService adminCatalogService;
    private final Grid<FilmAdminRow> filmGrid = new Grid<>(FilmAdminRow.class, false);
    private final Grid<User> userGrid = new Grid<>(User.class, false);

    public AdminDataView(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        configureFilmGrid();
        configureUserGrid();
        refreshGrids();

        Button addFilm = new Button("Add film", event -> addFilm());
        addFilm.addClassName("primary-action");

        Div filmsPanel = new Div(
                sectionTitle("Films", "Edit titles, ratings, and poster paths. Changes are saved to the database."),
                addFilm,
                filmGrid
        );
        filmsPanel.addClassName("surface-panel");

        Div usersPanel = new Div(
                sectionTitle("Accounts", "Enable or disable user accounts."),
                userGrid
        );
        usersPanel.addClassName("surface-panel");

        add(pageHero("Data admin", "Employee tools for catalog and account maintenance."), filmsPanel, usersPanel);
    }

    private void configureFilmGrid() {
        Editor<FilmAdminRow> editor = filmGrid.getEditor();
        Binder<FilmAdminRow> binder = new Binder<>(FilmAdminRow.class);
        editor.setBinder(binder);

        TextField titleField = new TextField();
        TextField genreField = new TextField();
        TextField ageField = new TextField();
        NumberField ratingField = new NumberField();
        NumberField durationField = new NumberField();
        TextField posterField = new TextField();

        filmGrid.addColumn(FilmAdminRow::getTitle).setHeader("Title").setEditorComponent(titleField);
        filmGrid.addColumn(FilmAdminRow::getGenre).setHeader("Genre").setEditorComponent(genreField);
        filmGrid.addColumn(FilmAdminRow::getAgeRating).setHeader("Age").setEditorComponent(ageField);
        filmGrid.addColumn(FilmAdminRow::getRating).setHeader("Rating").setEditorComponent(ratingField);
        filmGrid.addColumn(FilmAdminRow::getDurationMinutes).setHeader("Minutes").setEditorComponent(durationField);
        filmGrid.addColumn(FilmAdminRow::getPosterUrl).setHeader("Poster URL").setEditorComponent(posterField);

        binder.forField(titleField).asRequired().bind(FilmAdminRow::getTitle, FilmAdminRow::setTitle);
        binder.forField(genreField).bind(FilmAdminRow::getGenre, FilmAdminRow::setGenre);
        binder.forField(ageField).bind(FilmAdminRow::getAgeRating, FilmAdminRow::setAgeRating);
        binder.forField(ratingField).bind(FilmAdminRow::getRating, FilmAdminRow::setRating);
        binder.forField(durationField)
                .withConverter(Double::intValue, Integer::doubleValue)
                .bind(FilmAdminRow::getDurationMinutes, FilmAdminRow::setDurationMinutes);
        binder.forField(posterField).bind(FilmAdminRow::getPosterUrl, FilmAdminRow::setPosterUrl);

        filmGrid.addItemClickListener(event -> editor.editItem(event.getItem()));
        editor.addSaveListener(event -> {
            try {
                adminCatalogService.saveFilm(event.getItem());
                refreshGrids();
                Notification.show("Film saved");
            } catch (RuntimeException ex) {
                Notification.show(ex.getMessage());
            }
        });
        filmGrid.setWidthFull();
    }

    private void configureUserGrid() {
        userGrid.addColumn(User::getUsername).setHeader("Username");
        userGrid.addColumn(User::getEmail).setHeader("Email");
        userGrid.addColumn(User::getFullName).setHeader("Name");
        userGrid.addColumn(user -> user.getRole().name()).setHeader("Role");
        userGrid.addColumn(user -> user.getStatus().name()).setHeader("Status");
        userGrid.addColumn(user -> user.getCreatedAt() == null ? "" : user.getCreatedAt().toLocalDate().toString())
                .setHeader("Registered");
        userGrid.addComponentColumn(user -> {
            boolean active = user.getStatus() == UserStatus.ACTIVE;
            Button toggle = new Button(active ? "Disable" : "Enable", event -> toggleUser(user));
            toggle.addClassName(active ? "danger-action" : "secondary-action");
            return toggle;
        }).setHeader("Action");
        userGrid.setWidthFull();
    }

    private void addFilm() {
        FilmAdminRow draft = new FilmAdminRow(null, "New film", "Drama", "12A", 7.0, 100, "/images/posters/skyline-run.svg");
        adminCatalogService.saveFilm(draft);
        refreshGrids();
        Notification.show("New film added — click a row to edit");
    }

    private void toggleUser(User user) {
        try {
            UserStatus next = user.getStatus() == UserStatus.ACTIVE ? UserStatus.DISABLED : UserStatus.ACTIVE;
            adminCatalogService.setUserStatus(user.getUserId(), next);
            refreshGrids();
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private void refreshGrids() {
        filmGrid.setItems(adminCatalogService.listFilms());
        userGrid.setItems(adminCatalogService.listUsers());
    }

    private Div pageHero(String heading, String copy) {
        Span badge = new Span("Employee only");
        badge.addClassName("eyebrow");
        H2 title = new H2(heading);
        Paragraph description = new Paragraph(copy);
        Div hero = new Div(badge, title, description);
        hero.addClassName("page-hero");
        return hero;
    }

    private Div sectionTitle(String title, String caption) {
        H2 heading = new H2(title);
        Paragraph detail = new Paragraph(caption);
        Div wrapper = new Div(heading, detail);
        wrapper.addClassName("section-title");
        return wrapper;
    }
}
