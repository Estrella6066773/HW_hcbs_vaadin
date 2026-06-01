package com.hcbs.config;

/**
 * Local poster paths stored in {@code film.poster_url} (under {@code /images/posters/}).
 * Bundled artwork lives in {@code src/main/resources/META-INF/resources/images/posters/home/}.
 */
public final class FilmPosterCatalog {

    public static final String BASE = "/images/posters/";
    private static final String HOME = BASE + "home/";

    public static final String SPIRITED_AWAY = HOME + "spirited-away.png";
    public static final String LEGEND_OF_1900 = HOME + "legend-of-1900.png";
    public static final String WASTED_TIMES = HOME + "wasted-times.png";
    public static final String HARRY_POTTER_PHOENIX = HOME + "harry-potter-phoenix.png";
    public static final String SOLITUDE = HOME + "solitude.png";
    public static final String CALL_ME_BY_YOUR_NAME = HOME + "call-me-by-your-name.png";
    public static final String MALEFICENT = HOME + "maleficent.png";
    public static final String INCEPTION = HOME + "inception.png";
    public static final String CABIN_IN_THE_WOODS = HOME + "cabin-in-the-woods.png";
    public static final String ROCK_STAR = HOME + "rock-star.png";
    public static final String REIGN_OVER_ME = HOME + "reign-over-me.png";
    public static final String CIRQUE_DU_SOLEIL_O = HOME + "cirque-du-soleil-o.png";

    private FilmPosterCatalog() {
    }

    public static boolean isLocalPath(String posterUrl) {
        return posterUrl != null && posterUrl.startsWith(BASE);
    }
}
