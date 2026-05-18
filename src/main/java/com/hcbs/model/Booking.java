package com.hcbs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    private String bookingReference;

    @ManyToOne(optional = false)
    private Showing showing;

    @ManyToOne(optional = false)
    private User user;

    private LocalDateTime bookingDateTime;
    private int numberOfTickets;
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime cancellationDateTime;
    private BigDecimal cancellationCharge;
}
