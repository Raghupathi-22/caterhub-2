-- Production previously ran with the JVM in UTC while hibernate.jdbc.time_zone = Asia/Kolkata,
-- so Hibernate stored every LocalTime value 05:30 later than the value the app used
-- (e.g. 10:00 was stored as 15:30, and 18:25 as 23:55). The application now pins the JVM to
-- Asia/Kolkata, so stored values are no longer shifted; this restores existing rows to the
-- values users entered. Wraps at midnight; NULLs are left unchanged.
UPDATE service_requests
   SET start_time = SEC_TO_TIME(MOD(TIME_TO_SEC(start_time) + 86400 - 19800, 86400)),
       end_time   = SEC_TO_TIME(MOD(TIME_TO_SEC(end_time)   + 86400 - 19800, 86400));

UPDATE staffing_requests
   SET start_time = SEC_TO_TIME(MOD(TIME_TO_SEC(start_time) + 86400 - 19800, 86400)),
       end_time   = SEC_TO_TIME(MOD(TIME_TO_SEC(end_time)   + 86400 - 19800, 86400));

UPDATE worker_availabilities
   SET start_time = SEC_TO_TIME(MOD(TIME_TO_SEC(start_time) + 86400 - 19800, 86400)),
       end_time   = SEC_TO_TIME(MOD(TIME_TO_SEC(end_time)   + 86400 - 19800, 86400));

UPDATE menu_schedules
   SET start_time = SEC_TO_TIME(MOD(TIME_TO_SEC(start_time) + 86400 - 19800, 86400)),
       end_time   = SEC_TO_TIME(MOD(TIME_TO_SEC(end_time)   + 86400 - 19800, 86400));

UPDATE business_hours
   SET opening_time = SEC_TO_TIME(MOD(TIME_TO_SEC(opening_time) + 86400 - 19800, 86400)),
       closing_time = SEC_TO_TIME(MOD(TIME_TO_SEC(closing_time) + 86400 - 19800, 86400));

UPDATE notification_preferences
   SET quiet_hours_start = SEC_TO_TIME(MOD(TIME_TO_SEC(quiet_hours_start) + 86400 - 19800, 86400)),
       quiet_hours_end   = SEC_TO_TIME(MOD(TIME_TO_SEC(quiet_hours_end)   + 86400 - 19800, 86400));

UPDATE report_schedules
   SET scheduled_time = SEC_TO_TIME(MOD(TIME_TO_SEC(scheduled_time) + 86400 - 19800, 86400));