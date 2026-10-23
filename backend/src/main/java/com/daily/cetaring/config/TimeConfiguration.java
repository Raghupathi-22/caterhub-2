package com.daily.cetaring.config;

import com.daily.cetaring.shared.time.EventTimes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfiguration {

    /** Business clock (Asia/Kolkata) used for event-time eligibility; injectable so tests can fix "now". */
    @Bean
    public Clock businessClock() {
        return Clock.system(EventTimes.BUSINESS_ZONE);
    }
}
