package com.hcbs.repository;

import com.hcbs.model.City;
import com.hcbs.model.PriceRule;
import com.hcbs.model.SeatArea;
import com.hcbs.model.TimeBand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {
    Optional<PriceRule> findByCityAndTimeBandAndSeatArea(City city, TimeBand timeBand, SeatArea seatArea);
}
