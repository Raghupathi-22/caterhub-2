package com.daily.cetaring.features.booking.controller;

import com.daily.cetaring.features.booking.dto.CreateBookingRequest;
import com.daily.cetaring.features.booking.dto.BookingDTO;
import com.daily.cetaring.features.booking.dto.CreateMyBookingRequest;
import jakarta.validation.Valid;
import com.daily.cetaring.features.booking.service.BookingService;
import com.daily.cetaring.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final UserRepository userRepository;

    @PostMapping("/me")
    public ResponseEntity<BookingDTO> createMyBooking(
        Authentication authentication,
        @Valid @RequestBody CreateMyBookingRequest request
    ) {
        return new ResponseEntity<>(
            bookingService.createBookingForUsername(authentication.getName(), request),
            HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}/me")
    public ResponseEntity<BookingDTO> getMyBooking(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingForUsername(id, authentication.getName()));
    }

    @DeleteMapping("/{id}/me")
    public ResponseEntity<Void> cancelMyBooking(@PathVariable Long id, Authentication authentication) {
        bookingService.cancelBookingForUsername(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return new ResponseEntity<>(bookingService.createBooking(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBooking(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingDTO>> getMyBookings(Authentication authentication) {
        String username = authentication.getName();
        Long userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"))
            .getId();
        return ResponseEntity.ok(bookingService.getUserBookingsForHistory(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<BookingDTO>> getBusinessBookings(@PathVariable Long businessId) {
        return ResponseEntity.ok(bookingService.getBusinessBookings(businessId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
