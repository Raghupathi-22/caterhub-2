package com.daily.cetaring;

import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CetaringBackendApplicationTimeZoneTest {

    @Test
    void pinsJvmTimeZoneToHibernateJdbcTimeZoneEvenWhenHostIsUtc() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            CetaringBackendApplication.configureDefaultTimeZone();

            assertEquals("Asia/Kolkata", TimeZone.getDefault().getID());
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
