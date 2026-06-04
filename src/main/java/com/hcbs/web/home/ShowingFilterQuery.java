package com.hcbs.web.home;

import com.hcbs.dto.ShowingListingFilter;
import com.vaadin.flow.router.QueryParameters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Serialises {@link ShowingListingFilter} and booking context as URL query parameters.
 */
public final class ShowingFilterQuery {
    //不可被继承，就是工具类，所有方法都是 static

    public static final String PARAM_CITY = "city";
    public static final String PARAM_CINEMA = "cinema";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_SHOWING = "showingId";

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private ShowingFilterQuery() {
    }

    public static boolean hasCriteria(ShowingListingFilter filter) {
        return filter != null && !filter.isEmpty();
        //判断「用户是否真的在筛选」，而不是浏览全部影片。
    }

    public static QueryParameters toQueryParameters(ShowingListingFilter filter) {
        Map<String, List<String>> params = new HashMap<>();
        if (filter == null) {
            return new QueryParameters(params);
        }
        if (filter.cityId() != null) {
            params.put(PARAM_CITY, List.of(filter.cityId().toString()));
        }
        if (filter.cinemaId() != null) {
            params.put(PARAM_CINEMA, List.of(filter.cinemaId().toString()));
        }
        if (filter.date() != null) {
            params.put(PARAM_DATE, List.of(filter.date().format(ISO_DATE)));
        }
        if (filter.filmTitle() != null && !filter.filmTitle().isBlank()) {
            params.put(PARAM_TITLE, List.of(filter.filmTitle()));
        }
        return new QueryParameters(params);
    }

    public static QueryParameters withShowingId(ShowingListingFilter filter, Long showingId) {
        Map<String, List<String>> params = new HashMap<>(toQueryParameters(filter).getParameters());
        if (showingId != null) {
            params.put(PARAM_SHOWING, List.of(showingId.toString()));
        }
        return new QueryParameters(params);
    }

    public static ShowingListingFilter fromQueryParameters(QueryParameters queryParameters) {
        if (queryParameters == null) {
            return ShowingListingFilter.of(null, null, null, null);
        }
        Long cityId = parseLong(queryParameters, PARAM_CITY);
        Long cinemaId = parseLong(queryParameters, PARAM_CINEMA);
        LocalDate date = parseDate(queryParameters);
        String title = firstValue(queryParameters, PARAM_TITLE);
        return ShowingListingFilter.of(cityId, cinemaId, date, title);
    }

    public static Optional<Long> showingIdFrom(QueryParameters queryParameters) {
        return Optional.ofNullable(parseLong(queryParameters, PARAM_SHOWING));
    }

    private static Long parseLong(QueryParameters params, String key) {
        String raw = firstValue(params, key);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static LocalDate parseDate(QueryParameters params) {
        String raw = firstValue(params, PARAM_DATE);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim(), ISO_DATE);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String firstValue(QueryParameters params, String key) {
        List<String> values = params.getParameters().get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }
}
