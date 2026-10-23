package com.daily.cetaring.shared.time;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Event dates/times (event_date + start_time) are stored as Hyderabad wall-clock values without an offset,
 * while the JVM in production (Railway) runs in UTC. All "has this event started?" decisions must therefore
 * compare against the current time in the business zone, never the JVM default zone.
 */
public final class EventTimes {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    private EventTimes() {
    }

    public static LocalDateTime now(Clock clock) {
        return LocalDateTime.now(clock.withZone(BUSINESS_ZONE));
    }

    public static LocalDate today(Clock clock) {
        return now(clock).toLocalDate();
    }

    /**
     * True when the event's scheduled start is at or before {@code now}. A job is only offerable/acceptable
     * while its start is strictly in the future. A missing start time is treated as the start of the event day.
     */
    public static boolean hasStarted(LocalDate eventDate, LocalTime startTime, LocalDateTime now) {
        if (eventDate == null) {
            return false;
        }
        LocalDateTime start = eventDate.atTime(startTime == null ? LocalTime.MIDNIGHT : startTime);
        return !start.isAfter(now);
    }
}
