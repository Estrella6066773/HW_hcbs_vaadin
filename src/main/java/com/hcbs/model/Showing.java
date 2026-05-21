package com.hcbs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "screen_screen_id", "show_date", "start_time"
}))
@Getter
@Setter
@NoArgsConstructor
public class Showing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long showingId;

    @ManyToOne(optional = false)
    private Film film;

    @ManyToOne(optional = false)
    private Screen screen;

    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private TimeBand timeBand;

    @Enumerated(EnumType.STRING)
    private ShowingStatus status;

    public Showing(Film film, Screen screen, LocalDate showDate, LocalTime startTime, LocalTime endTime, TimeBand timeBand) {
        this.film = film;
        this.screen = screen;
        this.showDate = showDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeBand = timeBand;
        this.status = ShowingStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return film.getTitle() + " - " + showDate + " " + startTime;
    }
}
