package com.va1err.habittracker.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class TestClockConfig {

    @Bean
    @Primary
    public Clock customClock() {
        return Clock.fixed(
                Instant.parse("2026-07-21T09:00:00Z"),
                ZoneId.of("Europe/Moscow")
        );
    }

}
