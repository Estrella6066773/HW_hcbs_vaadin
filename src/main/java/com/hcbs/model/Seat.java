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

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"screen_screen_id", "seat_number"}))
@Getter
@Setter
@NoArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @ManyToOne(optional = false)
    private Screen screen;

    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatArea seatArea;

    public Seat(Screen screen, String seatNumber, SeatArea seatArea) {
        this.screen = screen;
        this.seatNumber = seatNumber;
        this.seatArea = seatArea;
    }

    @Override
    public String toString() {
        return seatNumber + " (" + seatArea + ")";
    }
}
