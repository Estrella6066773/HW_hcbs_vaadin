package com.hcbs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"showing_showing_id", "seat_seat_id"}))
@Getter
@Setter
@NoArgsConstructor
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingSeatId;

    @ManyToOne(optional = false)
    private Booking booking;

    @ManyToOne(optional = false)
    private Seat seat;

    @ManyToOne(optional = false)
    private Showing showing;

    private BigDecimal ticketPrice;

    public BookingSeat(Booking booking, Seat seat, Showing showing, BigDecimal ticketPrice) {
        this.booking = booking;
        this.seat = seat;
        this.showing = showing;
        this.ticketPrice = ticketPrice;
    }
}
