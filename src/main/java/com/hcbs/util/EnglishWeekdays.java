package com.hcbs.util;

import com.vaadin.flow.component.datepicker.DatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 界面日期控件的英文星期与月份文案（Mon–Sun，避免中文「周一」等）。
 * <p>
 * 使用处：排片表头（{@link #scheduleHeader}）、各页面的 {@link DatePicker}
 * （调用 {@link #configureDatePicker}）。与 {@link com.hcbs.config.VaadinLocaleConfiguration}
 * 配合，保证日历弹层与表头均为英文。
 */
public final class EnglishWeekdays {

    /**
     * Vaadin DatePicker 弹层顶部星期行顺序：周日为首列（与 {@link #buildDatePickerI18n} 中 firstDayOfWeek 配合）。
     */
    private static final List<String> CALENDAR_WEEKDAYS_SHORT =
            List.of("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");

    /** 排片网格列头，例如 {@code Mon 6/1}；EEE = 三字母星期，M/d = 月/日 */
    private static final DateTimeFormatter SCHEDULE_DAY_HEADER =
            DateTimeFormatter.ofPattern("EEE M/d", Locale.ENGLISH);

    /** 复用同一份 I18n，避免每个 DatePicker 重复构建列表 */
    private static final DatePicker.DatePickerI18n DATE_PICKER_I18N = buildDatePickerI18n();

    private EnglishWeekdays() {
    }

    /**
     * 管理端周排片、筛选面板等表头文案。
     *
     * @param date 列对应的日期
     * @return 英文缩写星期 + 月/日，如 {@code Wed 6/4}
     */
    public static String scheduleHeader(LocalDate date) {
        return SCHEDULE_DAY_HEADER.format(date);
    }

    /**
     * 统一配置 DatePicker：区域 + 自定义星期/月份英文标签。
     */
    public static void configureDatePicker(DatePicker picker) {
        picker.setLocale(Locale.ENGLISH);       // JVM/浏览器语言不影响时的兜底
        picker.setI18n(DATE_PICKER_I18N);       // 弹层内 Sun–Sat、January–December
    }

    private static DatePicker.DatePickerI18n buildDatePickerI18n() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setWeekdays(List.of(
                "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        i18n.setWeekdaysShort(CALENDAR_WEEKDAYS_SHORT);
        i18n.setMonthNames(List.of(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"));
        i18n.setFirstDayOfWeek(1); // 1 = Monday 为一周第一天（英国/业务常用）
        return i18n;
    }
}
