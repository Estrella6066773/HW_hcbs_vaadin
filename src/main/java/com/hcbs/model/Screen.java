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
public class Screen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long screenId;

    @ManyToOne(optional = false)
    private Cinema cinema;

    private int screenNumber;
    private int capacity;

    public Screen(Cinema cinema, int screenNumber, int capacity) {
        this.cinema = cinema;
        this.screenNumber = screenNumber;
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Screen " + screenNumber + " - " + cinema.getName();
    }
}
