package com.hcbs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking", indexes = {
        @Index(name = "idx_booking_customer", columnList = "customer_user_id"),
        @Index(name = "idx_booking_created_by", columnList = "created_by_user_id"),
        @Index(name = "idx_booking_showing", columnList = "showing_showing_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false, unique = true, length = 32)
    private String bookingReference;

    @ManyToOne(optional = false)
    private Showing showing;

    /** Staff or customer who performed the booking action. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    /** Customer who owns this order (may differ from createdBy when booked on behalf). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private LocalDateTime bookingDateTime;

    @Column(nullable = false)
    private int numberOfTickets;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    private LocalDateTime cancellationDateTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal cancellationCharge;
}
