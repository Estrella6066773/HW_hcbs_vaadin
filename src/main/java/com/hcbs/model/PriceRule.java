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

import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"city_city_id", "time_band", "seat_area"}))
@Getter
@Setter
@NoArgsConstructor
public class PriceRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priceRuleId;

    @ManyToOne(optional = false)
    private City city;

    @Enumerated(EnumType.STRING)
    private TimeBand timeBand;

    @Enumerated(EnumType.STRING)
    private SeatArea seatArea;

    private BigDecimal price;

    public PriceRule(City city, TimeBand timeBand, SeatArea seatArea, BigDecimal price) {
        this.city = city;
        this.timeBand = timeBand;
        this.seatArea = seatArea;
        this.price = price;
    }
}
