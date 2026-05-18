package com.hcbs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FilmActor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long filmActorId;

    @ManyToOne(optional = false)
    private Film film;

    @ManyToOne(optional = false)
    private Actor actor;

    public FilmActor(Film film, Actor actor) {
        this.film = film;
        this.actor = actor;
    }
}
