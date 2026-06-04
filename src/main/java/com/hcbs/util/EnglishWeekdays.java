package com.hcbs.util;

import com.vaadin.flow.component.datepicker.DatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * English weekday labels (Mon–Sun) for UI date controls and schedule headers.
 */
public final class EnglishWeekdays {

    /** Vaadin calendar header order: Sunday → Saturday */
    private static final List<String> CALENDAR_WEEKDAYS_SHORT =
            List.of("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");

    private static final DateTimeFormatter SCHEDULE_DAY_HEADER =
            DateTimeFormatter.ofPattern("EEE M/d", Locale.ENGLISH);

    private static final DatePicker.DatePickerI18n DATE_PICKER_I18N = buildDatePickerI18n();

    private EnglishWeekdays() {
    }

    /** e.g. {@code Mon 6/1} */
    public static String scheduleHeader(LocalDate date) {
        return SCHEDULE_DAY_HEADER.format(date);
    }

    public static void configureDatePicker(DatePicker picker) {
        picker.setLocale(Locale.ENGLISH);
        picker.setI18n(DATE_PICKER_I18N);
    }

    private static DatePicker.DatePickerI18n buildDatePickerI18n() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setWeekdays(List.of(
                "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        i18n.setWeekdaysShort(CALENDAR_WEEKDAYS_SHORT);
        i18n.setMonthNames(List.of(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"));
        i18n.setFirstDayOfWeek(1);
        return i18n;
    }
}
