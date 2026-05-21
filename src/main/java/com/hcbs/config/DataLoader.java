package com.hcbs.config;

import com.hcbs.repository.CityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    private final CityRepository cityRepository;
    private final HcbsTestDataSeeder testDataSeeder;

    public DataLoader(CityRepository cityRepository, HcbsTestDataSeeder testDataSeeder) {
        this.cityRepository = cityRepository;
        this.testDataSeeder = testDataSeeder;
    }

    @Override
    public void run(String... args) {
        if (cityRepository.count() > 0) {
            return;
        }
        testDataSeeder.seedAll();
    }
}
