package com.hcbs.config;

/**
 * Official poster image URLs stored in {@link com.hcbs.model.Film#posterUrl}.
 * UI loads them via {@code <img src="...">} only — no generated geometric placeholders.
 */
public final class FilmPosterCatalog {

    public static final String FALLBACK =
            "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/Movie_reel.svg/440px-Movie_reel.svg.png";

    public static final String SPIRITED_AWAY =
            "https://upload.wikimedia.org/wikipedia/en/d/db/Spirited_Away_Japanese_poster.png";
    public static final String LEGEND_OF_1900 =
            "https://upload.wikimedia.org/wikipedia/en/3/3e/Legend_of_1900_poster.jpg";
    /** No stable Commons poster for this seed title; reuse a neutral cinema still image. */
    public static final String WASTED_TIMES =
            "https://upload.wikimedia.org/wikipedia/commons/9/9e/Cinema_1.png";
    public static final String HARRY_POTTER_PHOENIX =
            "https://upload.wikimedia.org/wikipedia/en/9/98/Harry_Potter_and_the_Order_of_the_Phoenix_poster.jpg";
    public static final String SOLITUDE = FALLBACK;

    public static final String CALL_ME_BY_YOUR_NAME =
            "https://upload.wikimedia.org/wikipedia/en/9/9b/Call_Me_by_Your_Name_%282017_film%29.png";
    public static final String MALEFICENT =
            "https://upload.wikimedia.org/wikipedia/en/5/56/Maleficent_Poster.jpg";
    public static final String INCEPTION =
            "https://upload.wikimedia.org/wikipedia/en/2/e/e6/Inception_%282010%29_theatrical_poster.jpg";
    public static final String CABIN_IN_THE_WOODS =
            "https://upload.wikimedia.org/wikipedia/en/8/8b/The_Cabin_in_the_Woods_POSTER.jpg";
    public static final String ROCK_STAR =
            "https://upload.wikimedia.org/wikipedia/en/1/17/Rock_Star_film.jpg";
    public static final String REIGN_OVER_ME =
            "https://upload.wikimedia.org/wikipedia/en/3/3c/Reign_Over_Me.jpg";
    public static final String CIRQUE_DU_SOLEIL_O =
            "https://upload.wikimedia.org/wikipedia/en/1/1e/Cirque_du_Soleil_-_O_%28poster%29.jpg";

    private FilmPosterCatalog() {
    }
}
