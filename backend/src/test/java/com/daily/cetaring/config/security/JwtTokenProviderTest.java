package com.daily.cetaring.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JwtTokenProviderTest {

    @Test
    void refreshTokensIssuedInTheSameSecondAreUnique() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "test-secret-test-secret-test-secret-test-secret-0123456789");
        ReflectionTestUtils.setField(provider, "jwtExpiration", 60_000L);
        ReflectionTestUtils.setField(provider, "refreshExpiration", 600_000L);

        String first = provider.generateRefreshToken("admin");
        String second = provider.generateRefreshToken("admin");

        assertNotEquals(first, second);
        assertEquals("admin", provider.getUsernameFromToken(second));
    }
}
