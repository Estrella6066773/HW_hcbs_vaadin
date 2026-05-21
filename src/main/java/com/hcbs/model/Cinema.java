package com.hcbs.model;

import jakarta.persistence.Column;
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

@Entity
@Table(name = "cinema", uniqueConstraints = @UniqueConstraint(columnNames = {"city_city_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
public class Cinema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cinemaId;

    @ManyToOne(optional = false)
    private City city;

    private String name;
    private String location;

    public Cinema(City city, String name, String location) {
        this.city = city;
        this.name = name;
        this.location = location;
    }

    @Override
    public String toString() {
        return name + " (" + city.getName() + ")";
    }
}
