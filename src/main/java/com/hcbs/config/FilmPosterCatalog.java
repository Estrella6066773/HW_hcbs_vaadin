package com.hcbs.config;

/**
 * Local poster paths stored in {@code film.poster_url} (under {@code /images/posters/}).
 * Replace JPEG files in {@code src/main/resources/META-INF/resources/images/posters/}.
 */
public final class FilmPosterCatalog {

    public static final String BASE = "/images/posters/";

    public static final String SPIRITED_AWAY = BASE + "spirited-away.jpg";
    public static final String LEGEND_OF_1900 = BASE + "legend-of-1900.jpg";
    public static final String WASTED_TIMES = BASE + "wasted-times.jpg";
    public static final String HARRY_POTTER_PHOENIX = BASE + "harry-potter-phoenix.jpg";
    /** Intentionally no bundled file — demonstrates missing poster UI. */
    public static final String SOLITUDE = BASE + "solitude.jpg";
    public static final String CALL_ME_BY_YOUR_NAME = BASE + "call-me-by-your-name.jpg";
    public static final String MALEFICENT = BASE + "maleficent.jpg";
    public static final String INCEPTION = BASE + "inception.jpg";
    public static final String CABIN_IN_THE_WOODS = BASE + "cabin-in-the-woods.jpg";
    public static final String ROCK_STAR = BASE + "rock-star.jpg";
    public static final String REIGN_OVER_ME = BASE + "reign-over-me.jpg";
    public static final String CIRQUE_DU_SOLEIL_O = BASE + "cirque-du-soleil-o.jpg";

    private FilmPosterCatalog() {
    }

    public static boolean isLocalPath(String posterUrl) {
        return posterUrl != null && posterUrl.startsWith(BASE);
    }
}
