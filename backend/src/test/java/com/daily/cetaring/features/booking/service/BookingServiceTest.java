package com.daily.cetaring.features.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.daily.cetaring.features.booking.repository.BookingRepository;
import com.daily.cetaring.features.booking.entity.Booking;
import com.daily.cetaring.features.booking.dto.CreateBookingRequest;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.daily.cetaring.shared.repository.BusinessRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import java.util.Optional;
import static org.mockito.Mockito.when;

public class BookingServiceTest {
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingService = new BookingService(bookingRepository, userRepository, businessRepository);
    }

    @Test
    public void testCreateBooking() {
        CreateBookingRequest request = CreateBookingRequest.builder()
            .businessId(1L)
            .userId(1L)
            .eventType("Birthday")
            .guestCount(50)
            .mealType("Lunch")
            .eventDateTime(LocalDateTime.now().plusDays(7))
            .deliveryAddress("123 Main St")
            .estimatedAmount(new BigDecimal("5000"))
            .build();

        assertNotNull(request);
        assertEquals(50, request.getGuestCount());
        assertEquals("Birthday", request.getEventType());
    }

    @Test
    public void testGetBooking() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            bookingService.getBooking(999L);
        });
    }
}
