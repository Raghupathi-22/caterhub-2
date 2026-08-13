package com.daily.cetaring.features.booking.service;

import com.daily.cetaring.features.booking.entity.Booking;
import com.daily.cetaring.features.booking.repository.BookingRepository;
import com.daily.cetaring.features.booking.dto.CreateBookingRequest;
import com.daily.cetaring.features.booking.dto.CreateMyBookingRequest;
import com.daily.cetaring.features.booking.dto.BookingDTO;
import com.daily.cetaring.shared.repository.BusinessRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import com.daily.cetaring.shared.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    public BookingDTO createBookingForUsername(String username, CreateMyBookingRequest request) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        Long businessId = request.getBusinessId() != null
            ? request.getBusinessId()
            : businessRepository.findFirstByIsActiveTrueAndDeletedAtIsNullOrderByIdAsc()
                .orElseThrow(() -> new IllegalArgumentException("No active catering business is available"))
                .getId();
        CreateBookingRequest internalRequest = CreateBookingRequest.builder()
            .businessId(businessId)
            .userId(user.getId())
            .eventType(request.getEventType())
            .guestCount(request.getGuestCount())
            .mealType(request.getMealType())
            .eventDateTime(request.getEventDateTime())
            .deliveryAddress(request.getDeliveryAddress())
            .specialInstructions(request.getSpecialInstructions())
            .estimatedAmount(request.getEstimatedAmount())
            .build();
        return createBooking(internalRequest);
    }

    public BookingDTO getBookingForUsername(Long bookingId, String username) {
        BookingDTO booking = getBooking(bookingId);
        Long userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"))
            .getId();
        if (!userId.equals(booking.getUserId())) {
            throw new IllegalArgumentException("Booking not found");
        }
        return booking;
    }

    public void cancelBookingForUsername(Long bookingId, String username) {
        getBookingForUsername(bookingId, username);
        cancelBooking(bookingId);
    }

    public BookingDTO createBooking(CreateBookingRequest request) {
        Long businessId = request.getBusinessId() != null
            ? request.getBusinessId()
            : businessRepository.findFirstByIsActiveTrueAndDeletedAtIsNullOrderByIdAsc()
                .orElseThrow(() -> new IllegalArgumentException("No active catering business is available"))
                .getId();
        Booking booking = Booking.builder()
            .businessId(businessId)
            .userId(request.getUserId())
            .eventType(request.getEventType())
            .guestCount(request.getGuestCount())
            .mealType(request.getMealType())
            .eventDate(request.getEventDateTime().toLocalDate())
            .eventDateTime(request.getEventDateTime())
            .deliveryAddress(request.getDeliveryAddress())
            .specialInstructions(request.getSpecialInstructions())
            .subtotalAmount(request.getEstimatedAmount())
            .taxAmount(BigDecimal.ZERO)
            .deliveryFee(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(request.getEstimatedAmount())
            .status("PENDING")
            .paymentStatus("PENDING")
            .build();

        booking = bookingRepository.save(booking);
        return mapToDTO(booking);
    }

    public BookingDTO getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .map(this::mapToDTO)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public List<BookingDTO> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public List<BookingDTO> getUserBookingsForHistory(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public List<BookingDTO> getBusinessBookings(Long businessId) {
        return bookingRepository.findByBusinessId(businessId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public BookingDTO updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        return mapToDTO(booking);
    }

    public void cancelBooking(Long bookingId) {
        BookingDTO booking = getBooking(bookingId);
        if (!"CANCELLED".equals(booking.getStatus())) {
            updateBookingStatus(bookingId, "CANCELLED");
        }
    }

    private BookingDTO mapToDTO(Booking booking) {
        return BookingDTO.builder()
            .id(booking.getId())
            .businessId(booking.getBusinessId())
            .userId(booking.getUserId())
            .bookingReference(booking.getBookingReference())
            .eventType(booking.getEventType())
            .guestCount(booking.getGuestCount())
            .mealType(booking.getMealType())
            .eventDateTime(booking.getEventDateTime())
            .deliveryAddress(booking.getDeliveryAddress())
            .specialInstructions(booking.getSpecialInstructions())
            .totalAmount(booking.getTotalAmount())
            .status(booking.getStatus())
            .paymentStatus(booking.getPaymentStatus())
            .createdAt(booking.getCreatedAt())
            .build();
    }
}
