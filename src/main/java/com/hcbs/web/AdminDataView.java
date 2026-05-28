package com.hcbs.web;

import com.hcbs.dto.FilmAdminRow;
import com.hcbs.dto.ShowingRow;
import com.hcbs.model.Cinema;
import com.hcbs.model.Screen;
import com.hcbs.model.User;
import com.hcbs.model.UserStatus;
import com.hcbs.service.admin.AdminCatalogService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Data admin")
@RolesAllowed({"ADMIN", "MANAGER"})
public class AdminDataView extends VerticalLayout {

    private static final LocalTime SCHEDULE_START = LocalTime.of(9, 0);
    private static final LocalTime SCHEDULE_END = LocalTime.of(23, 0);
    private static final double MIN_SHOWING_BLOCK_PERCENT = 24.0;

    private final AdminCatalogService adminCatalogService;
    private final Grid<User> userGrid = new Grid<>(User.class, false);
    private final TextField customerPhoneSearch = new TextField("Customer phone");
    private final DatePicker scheduleDate = new DatePicker("Schedule date");
    private final ComboBox<Cinema> cinemaFilter = new ComboBox<>("Cinemas");
    private final Div scheduleHost = new Div();
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE M/d");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private Button editSelected;
    private Button deleteSelected;
    private ShowingRow selectedShowing;

    public AdminDataView(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        configureSchedulePicker();
        configureCinemaFilter();
        configureUserGrid();
        refreshGrids();

        Button addShowing = new Button("Add showing", event -> openShowingDialog(null));
        addShowing.addClassName("primary-action");
        editSelected = new Button("Edit selected", event -> {
            if (selectedShowing == null) {
                Notification.show("Select a showing first");
                return;
            }
            openShowingDialog(selectedShowing);
        });
        editSelected.addClassName("secondary-action");
        deleteSelected = new Button("Delete selected", event -> {
            if (selectedShowing == null) {
                Notification.show("Select a showing first");
                return;
            }
            deleteShowing(selectedShowing);
        });
        deleteSelected.addClassName("danger-action");

        Div buttonRow = new Div(addShowing, editSelected, deleteSelected);
        buttonRow.addClassName("admin-button-row");

        Div scheduleActions = new Div(scheduleDate, buttonRow);
        scheduleActions.addClassName("admin-schedule-actions");
        scheduleHost.addClassName("admin-schedule");

        Div filmsPanel = new Div(
                sectionTitle("Films", "Select cinemas, choose a date, then manage weekly showings in the schedule."),
                scheduleActions,
                cinemaFilter,
                scheduleHost
        );
        filmsPanel.addClassName("surface-panel");

        Div usersPanel = new Div(
                sectionTitle("Accounts", "Enable or disable user accounts."),
                customerPhoneSearch,
                userGrid
        );
        usersPanel.addClassName("surface-panel");

        add(pageHero("Data admin", "Employee tools for catalog and account maintenance."), filmsPanel, usersPanel);
        updateSelectedButtons();
    }

    private void configureSchedulePicker() {
        scheduleDate.setValue(LocalDate.now());
        scheduleDate.setClearButtonVisible(false);
        scheduleDate.setHelperText("Pick a date to jump to its week.");
        scheduleDate.setWidth("220px");
        scheduleDate.addValueChangeListener(event -> {
            selectedShowing = null;
            refreshSchedule();
            scrollScheduleIntoView();
        });
    }

    private void configureCinemaFilter() {
        List<Cinema> cinemas = adminCatalogService.listCinemas();
        cinemaFilter.setItems(cinemas);
        cinemaFilter.setItemLabelGenerator(cinema -> cinema.getName() + " (" + cinema.getCity().getName() + ")");
        cinemaFilter.setPlaceholder("Search or select cinemas");
        cinemaFilter.setClearButtonVisible(true);
        cinemaFilter.setWidth("min(100%, 360px)");
        if (!cinemas.isEmpty()) {
            cinemaFilter.setValue(cinemas.get(0));
        }
        cinemaFilter.addClassName("admin-cinema-filter");
        cinemaFilter.addValueChangeListener(event -> {
            selectedShowing = null;
            refreshSchedule();
        });
    }

    private void configureUserGrid() {
        userGrid.addColumn(User::getUsername).setHeader("Username");
        userGrid.addColumn(user -> valueOrBlank(user.getPhone())).setHeader("Phone number");
        userGrid.addColumn(user -> valueOrBlank(user.getEmail())).setHeader("Email");
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

        customerPhoneSearch.setPlaceholder("Search customer phone");
        customerPhoneSearch.setHelperText("Filters customer accounts by phone prefix");
        customerPhoneSearch.setClearButtonVisible(true);
        customerPhoneSearch.setValueChangeMode(ValueChangeMode.LAZY);
        customerPhoneSearch.setWidth("min(100%, 360px)");
        customerPhoneSearch.getElement().setAttribute("autocomplete", "tel");
        customerPhoneSearch.addValueChangeListener(event -> refreshUsers());
    }

    private void openShowingDialog(ShowingRow showing) {
        Dialog dialog = new Dialog();
        boolean editing = showing != null;
        dialog.setHeaderTitle(editing ? "Edit showing" : "Add showing");

        List<FilmAdminRow> films = adminCatalogService.listFilms();
        List<Screen> screens = adminCatalogService.listScreens();

        ComboBox<FilmAdminRow> film = new ComboBox<>("Film");
        film.setItems(films);
        film.setItemLabelGenerator(FilmAdminRow::getTitle);
        film.setWidthFull();

        ComboBox<Screen> screen = new ComboBox<>("Cinema and screen");
        screen.setItems(screens);
        screen.setItemLabelGenerator(item -> item.getCinema().getName() + " - Screen " + item.getScreenNumber());
        screen.setWidthFull();

        DatePicker date = new DatePicker("Date");
        date.setValue(editing ? showing.showDate() : scheduleDate.getValue());
        date.setWidthFull();

        TimePicker start = new TimePicker("Start time");
        start.setStep(Duration.ofMinutes(5));
        start.setValue(editing ? showing.startTime() : LocalTime.of(18, 30));
        start.setWidthFull();

        if (editing) {
            films.stream()
                    .filter(item -> item.getFilmId().equals(showing.filmId()))
                    .findFirst()
                    .ifPresent(film::setValue);
            screens.stream()
                    .filter(item -> item.getScreenId().equals(showing.screenId()))
                    .findFirst()
                    .ifPresent(screen::setValue);
        }

        Button save = new Button(editing ? "Save changes" : "Add showing", event -> {
            try {
                if (film.getValue() == null || screen.getValue() == null) {
                    Notification.show("Choose a film and screen");
                    return;
                }
                if (editing) {
                    adminCatalogService.updateShowing(
                            showing.showingId(),
                            film.getValue().getFilmId(),
                            screen.getValue().getScreenId(),
                            date.getValue(),
                            start.getValue());
                } else {
                    adminCatalogService.addShowing(
                            film.getValue().getFilmId(),
                            screen.getValue().getScreenId(),
                            date.getValue(),
                            start.getValue());
                }
                scheduleDate.setValue(date.getValue());
                selectedShowing = null;
                refreshSchedule();
                dialog.close();
                Notification.show(editing ? "Showing updated" : "Showing added");
            } catch (RuntimeException ex) {
                Notification.show(ex.getMessage());
            }
        });
        save.addClassName("primary-action");
        Button cancel = new Button("Cancel", event -> dialog.close());
        cancel.addClassName("secondary-action");

        Div actions = new Div(save, cancel);
        actions.addClassName("admin-button-row");
        VerticalLayout form = new VerticalLayout(film, screen, date, start, actions);
        form.setPadding(false);
        form.setSpacing(true);
        dialog.add(form);
        dialog.open();
    }

    private void deleteShowing(ShowingRow showing) {
        try {
            adminCatalogService.deleteShowing(showing.showingId());
            selectedShowing = null;
            refreshSchedule();
            Notification.show("Showing deleted");
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private void refreshSchedule() {
        scheduleHost.removeAll();
        updateSelectedButtons();

        LocalDate selectedDate = scheduleDate.getValue() == null ? LocalDate.now() : scheduleDate.getValue();
        LocalDate weekStart = selectedDate.with(DayOfWeek.MONDAY);
        List<LocalDate> days = weekStart.datesUntil(weekStart.plusDays(7)).toList();
        Cinema selectedCinema = cinemaFilter.getValue();
        List<Screen> cinemaScreens = adminCatalogService.listScreens().stream()
                .filter(screen -> selectedCinema != null
                        && screen.getCinema().getCinemaId().equals(selectedCinema.getCinemaId()))
                .sorted(Comparator.comparing((Screen screen) -> screen.getCinema().getName())
                        .thenComparing(Screen::getScreenNumber))
                .toList();
        List<ShowingRow> showings = adminCatalogService.listShowings().stream()
                .filter(showing -> !showing.showDate().isBefore(weekStart)
                        && showing.showDate().isBefore(weekStart.plusDays(7)))
                .filter(showing -> cinemaIdFor(showing, cinemaScreens) != null)
                .sorted(Comparator.comparing(ShowingRow::startTime))
                .toList();
        List<Screen> screens = cinemaScreens.stream()
                .filter(screen -> showings.stream().anyMatch(showing -> showing.screenId().equals(screen.getScreenId())))
                .toList();

        scheduleHost.add(timeRail(), dayHeaders(days, selectedDate));

        if (selectedCinema == null) {
            Div empty = new Div(new Span("Select a cinema to view the weekly schedule."));
            empty.addClassName("admin-schedule-empty");
            scheduleHost.add(empty);
            return;
        }
        if (screens.isEmpty()) {
            Div empty = new Div(new Span("No showings scheduled this week for " + selectedCinema.getName() + "."));
            empty.addClassName("admin-schedule-empty");
            scheduleHost.add(empty);
            return;
        }

        for (Screen screen : screens) {
            Div label = new Div(
                    new Span(screen.getCinema().getName()),
                    new Span("Screen " + screen.getScreenNumber())
            );
            label.addClassName("admin-screen-label");
            scheduleHost.add(label);

            for (LocalDate day : days) {
                Div cell = new Div();
                cell.addClassName("admin-schedule-cell");
                if (day.equals(selectedDate)) {
                    cell.addClassName("admin-selected-date");
                }
                showings.stream()
                        .filter(showing -> showing.screenId().equals(screen.getScreenId()) && showing.showDate().equals(day))
                        .forEach(showing -> cell.add(showingBlock(showing, day.equals(selectedDate))));
                scheduleHost.add(cell);
            }
        }
    }

    private Long cinemaIdFor(ShowingRow showing, List<Screen> screens) {
        return screens.stream()
                .filter(screen -> screen.getScreenId().equals(showing.screenId()))
                .map(screen -> screen.getCinema().getCinemaId())
                .findFirst()
                .orElse(null);
    }

    private Div timeRail() {
        Div rail = new Div(new Span("Screen"), new Span("09:00-23:00"));
        rail.addClassName("admin-time-rail");
        return rail;
    }

    private Div dayHeaders(List<LocalDate> days, LocalDate selectedDate) {
        Div headers = new Div();
        headers.addClassName("admin-day-headers");
        for (LocalDate day : days) {
            Span label = new Span(dayFormatter.format(day));
            if (day.equals(selectedDate)) {
                label.addClassName("admin-selected-date-header");
            }
            headers.add(label);
        }
        return headers;
    }

    private Div showingBlock(ShowingRow showing, boolean onSelectedDate) {
        Div block = new Div(
                new Span(timeFormatter.format(showing.startTime()) + "-" + timeFormatter.format(showing.endTime())),
                new Span(showing.filmTitle())
        );
        block.addClassName("admin-showing-block");
        block.addClassName("admin-showing-color-" + Math.floorMod(showing.filmId().intValue(), 6));
        if (onSelectedDate) {
            block.addClassName("admin-showing-on-selected-date");
        }
        if (selectedShowing != null && selectedShowing.showingId().equals(showing.showingId())) {
            block.addClassName("admin-showing-selected");
        }
        double height = scheduleHeightPercent(showing.startTime(), showing.endTime());
        block.getStyle().set("top", scheduleTopPercent(showing.startTime(), height) + "%");
        block.getStyle().set("height", height + "%");
        block.addClickListener(event -> {
            selectedShowing = showing;
            refreshSchedule();
        });
        return block;
    }

    private void scrollScheduleIntoView() {
        scheduleHost.getElement().executeJs("this.scrollIntoView({behavior: 'smooth', block: 'start'});");
    }

    private double scheduleTopPercent(LocalTime time, double heightPercent) {
        long minutes = Duration.between(SCHEDULE_START, clamp(time, SCHEDULE_START, SCHEDULE_END)).toMinutes();
        double top = minutes * 100.0 / Duration.between(SCHEDULE_START, SCHEDULE_END).toMinutes();
        return Math.min(top, Math.max(0, 100.0 - heightPercent - 8.0));
    }

    private double scheduleHeightPercent(LocalTime start, LocalTime end) {
        long minutes = Duration.between(
                clamp(start, SCHEDULE_START, SCHEDULE_END),
                clamp(end, SCHEDULE_START, SCHEDULE_END)).toMinutes();
        return Math.max(MIN_SHOWING_BLOCK_PERCENT,
                minutes * 100.0 / Duration.between(SCHEDULE_START, SCHEDULE_END).toMinutes());
    }

    private LocalTime clamp(LocalTime time, LocalTime min, LocalTime max) {
        if (time.isBefore(min)) {
            return min;
        }
        if (time.isAfter(max)) {
            return max;
        }
        return time;
    }

    private void updateSelectedButtons() {
        if (editSelected != null) {
            editSelected.setEnabled(selectedShowing != null);
        }
        if (deleteSelected != null) {
            deleteSelected.setEnabled(selectedShowing != null);
        }
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
        refreshSchedule();
        refreshUsers();
    }

    private void refreshUsers() {
        userGrid.setItems(adminCatalogService.listUsersByCustomerPhone(customerPhoneSearch.getValue()));
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value;
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
