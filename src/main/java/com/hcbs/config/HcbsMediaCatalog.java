package com.hcbs.config;

import com.hcbs.model.TimeBand;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended films, cast, and showtimes for the demo database.
 * Case-study anchor and boundary showings remain in {@link HcbsTestDataSeeder}.
 */
public final class HcbsMediaCatalog {

    private HcbsMediaCatalog() {
    }

    public static List<FilmSeed> extendedFilms() {
        return List.of(
                new FilmSeed("Midnight", "Call Me by Your Name",
                        "A night-shift driver uncovers a smuggling ring along the Thames.",
                        "Thriller", "15", 4.5, 110, "/images/posters/home/call-me-by-your-name.png"),
                new FilmSeed("Velvet", "Maleficent",
                        "Two musicians reunite on a cross-country train journey.",
                        "Romance", "12A", 4.2, 102, "/images/posters/home/maleficent.png"),
                new FilmSeed("Iron", "Inception",
                        "An ex-pilot is pulled back into service for one final storm rescue.",
                        "Action", "12A", 4.7, 125, "/images/posters/home/inception.png"),
                new FilmSeed("Lantern", "The Cabin in the Woods",
                        "Animated fable about village lanterns that guide lost travellers.",
                        "Animation", "U", 4.8, 98, "/images/posters/home/cabin-in-the-woods.png"),
                new FilmSeed("Quiet", "Rock Star",
                        "A hotel night clerk discovers guests who never checked out.",
                        "Horror", "15", 4.0, 99, "/images/posters/home/rock-star.png"),
                new FilmSeed("North", "Reign Over Me",
                        "Teenagers hike the Brecon Beacons and find a hidden valley.",
                        "Adventure", "12A", 4.3, 115, "/images/posters/home/reign-over-me.png"),
                new FilmSeed("Paper", "Cirque du Soleil: O",
                        "A satirical comedy about a local council election in a seaside town.",
                        "Comedy", "12", 4.1, 108, "/images/posters/home/cirque-du-soleil-o.png")
        );
    }

    public static List<ActorSeed> extendedActors() {
        return List.of(
                new ActorSeed("Elena Voss", "Stage and screen lead"),
                new ActorSeed("James Porter", "Character actor"),
                new ActorSeed("Priya Shah", "Lead actor"),
                new ActorSeed("Owen Blake", "Action specialist"),
                new ActorSeed("Zara Quinn", "Voice and live-action"),
                new ActorSeed("Theo Marsh", "Ensemble performer"),
                new ActorSeed("Chloe West", "Rising lead"),
                new ActorSeed("Felix Dunn", "Comedy and drama")
        );
    }

    /** filmKey → actor full names (must exist in base or extended actor pools). */
    public static List<CastSeed> extendedCast() {
        return List.of(
                new CastSeed("Midnight", "Elena Voss"),
                new CastSeed("Midnight", "James Porter"),
                new CastSeed("Velvet", "Priya Shah"),
                new CastSeed("Velvet", "Leo Grant"),
                new CastSeed("Iron", "Owen Blake"),
                new CastSeed("Iron", "Maya Stone"),
                new CastSeed("Lantern", "Zara Quinn"),
                new CastSeed("Lantern", "Nina Clark"),
                new CastSeed("Quiet", "Chloe West"),
                new CastSeed("Quiet", "Theo Marsh"),
                new CastSeed("North", "Felix Dunn"),
                new CastSeed("North", "Sam Reed"),
                new CastSeed("Paper", "Felix Dunn"),
                new CastSeed("Paper", "James Porter")
        );
    }

    /** Extra showings appended after case-study / test scenarios. */
    public static List<ShowingSeed> extendedShowings(LocalDate today) {
        List<ShowingSeed> specs = new ArrayList<>();

        specs.add(new ShowingSeed("Midnight", "London-Central", 4, today.plusDays(2),
                LocalTime.of(21, 0), LocalTime.of(22, 50), TimeBand.EVENING, "London thriller"));
        specs.add(new ShowingSeed("Velvet", "London-Central", 1, today.plusDays(4),
                LocalTime.of(19, 15), LocalTime.of(21, 0), TimeBand.EVENING, "London romance"));
        specs.add(new ShowingSeed("Iron", "London-East", 2, today.plusDays(5),
                LocalTime.of(18, 0), LocalTime.of(20, 5), TimeBand.EVENING, "London action"));
        specs.add(new ShowingSeed("Paper", "London-Central", 2, today.plusDays(6),
                LocalTime.of(13, 0), LocalTime.of(14, 48), TimeBand.AFTERNOON, "London comedy"));
        specs.add(new ShowingSeed("Lantern", "London-East", 1, today.plusDays(4),
                LocalTime.of(10, 30), LocalTime.of(12, 8), TimeBand.MORNING, "London animation"));

        specs.add(new ShowingSeed("North", "Birmingham-Bullring", 3, today.plusDays(3),
                LocalTime.of(16, 0), LocalTime.of(17, 55), TimeBand.AFTERNOON, "Birmingham adventure"));
        specs.add(new ShowingSeed("Quiet", "Birmingham-Bullring", 4, today.plusDays(5),
                LocalTime.of(21, 30), LocalTime.of(23, 9), TimeBand.EVENING, "Birmingham horror"));
        specs.add(new ShowingSeed("Velvet", "Birmingham-NewStreet", 2, today.plusDays(6),
                LocalTime.of(18, 45), LocalTime.of(20, 27), TimeBand.EVENING, "Birmingham romance"));
        specs.add(new ShowingSeed("Midnight", "Birmingham-NewStreet", 1, today.plusDays(2),
                LocalTime.of(20, 0), LocalTime.of(21, 50), TimeBand.EVENING, "Birmingham thriller"));

        specs.add(new ShowingSeed("Iron", "Bristol-Harbour", 3, today.plusDays(4),
                LocalTime.of(17, 30), LocalTime.of(19, 35), TimeBand.EVENING, "Bristol action"));
        specs.add(new ShowingSeed("Paper", "Bristol-Clifton", 2, today.plusDays(3),
                LocalTime.of(14, 15), LocalTime.of(16, 3), TimeBand.AFTERNOON, "Bristol comedy"));
        specs.add(new ShowingSeed("Lantern", "Bristol-Harbour", 4, today.plusDays(6),
                LocalTime.of(11, 0), LocalTime.of(12, 38), TimeBand.MORNING, "Bristol animation"));
        specs.add(new ShowingSeed("Quiet", "Bristol-Clifton", 1, today.plusDays(7),
                LocalTime.of(22, 0), LocalTime.of(23, 39), TimeBand.EVENING, "Bristol horror max day"));

        specs.add(new ShowingSeed("North", "Cardiff-Central", 2, today.plusDays(5),
                LocalTime.of(15, 30), LocalTime.of(17, 25), TimeBand.AFTERNOON, "Cardiff adventure"));
        specs.add(new ShowingSeed("Midnight", "Cardiff-Bay", 3, today.plusDays(4),
                LocalTime.of(20, 30), LocalTime.of(22, 20), TimeBand.EVENING, "Cardiff thriller"));
        specs.add(new ShowingSeed("Velvet", "Cardiff-Central", 1, today.plusDays(3),
                LocalTime.of(12, 0), LocalTime.of(13, 42), TimeBand.AFTERNOON, "Cardiff romance"));
        specs.add(new ShowingSeed("Paper", "Cardiff-Bay", 4, today.plusDays(7),
                LocalTime.of(19, 0), LocalTime.of(20, 48), TimeBand.EVENING, "Cardiff comedy max day"));
        specs.add(new ShowingSeed("Iron", "Cardiff-Bay", 1, today.plusDays(2),
                LocalTime.of(18, 15), LocalTime.of(20, 20), TimeBand.EVENING, "Cardiff action"));
        specs.add(new ShowingSeed("Lantern", "Cardiff-Central", 1, today.plusDays(5),
                LocalTime.of(9, 45), LocalTime.of(11, 23), TimeBand.MORNING, "Cardiff animation"));

        // Cross-city variety for search filters
        specs.add(new ShowingSeed("Harbour", "London-East", 2, today.plusDays(5),
                LocalTime.of(16, 30), LocalTime.of(18, 15), TimeBand.AFTERNOON, "London drama"));
        specs.add(new ShowingSeed("Orbit", "Birmingham-Bullring", 1, today.plusDays(6),
                LocalTime.of(10, 0), LocalTime.of(12, 15), TimeBand.MORNING, "Birmingham sci-fi"));
        specs.add(new ShowingSeed("Coral", "Bristol-Harbour", 2, today.plusDays(2),
                LocalTime.of(13, 30), LocalTime.of(15, 5), TimeBand.AFTERNOON, "Bristol family"));
        specs.add(new ShowingSeed("Archive", "London-Central", 4, today.plusDays(5),
                LocalTime.of(8, 45), LocalTime.of(10, 13), TimeBand.MORNING, "London documentary"));

        return specs;
    }

    public record FilmSeed(
            String key,
            String title,
            String description,
            String genre,
            String ageRating,
            double rating,
            int durationMinutes,
            String posterUrl
    ) {
    }

    public record ActorSeed(String fullName, String details) {
    }

    public record CastSeed(String filmKey, String actorFullName) {
    }

    public record ShowingSeed(
            String filmKey,
            String cinemaKey,
            int screenNumber,
            LocalDate showDate,
            LocalTime start,
            LocalTime end,
            TimeBand timeBand,
            String scenarioNote
    ) {
    }
}
