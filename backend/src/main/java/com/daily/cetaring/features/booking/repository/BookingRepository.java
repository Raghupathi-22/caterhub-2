package com.daily.cetaring.features.booking.repository;

import com.daily.cetaring.features.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByBusinessId(Long businessId);

    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Booking> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.businessId = :businessId AND b.status = 'CONFIRMED' " +
           "AND b.eventDate BETWEEN :startDate AND :endDate")
    List<Booking> findConfirmedBookingsByDateRange(@Param("businessId") Long businessId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status IN ('CONFIRMED', 'PREPARING', 'READY')")
    List<Booking> findUpcomingBookings(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status = 'DELIVERED' " +
           "ORDER BY b.deliveredAt DESC")
    Page<Booking> findCompletedBookings(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.businessId = :businessId AND b.status = 'DELIVERED' " +
           "AND DATE(b.createdAt) = :date")
    Long countDeliveredBookingsByDate(@Param("businessId") Long businessId, @Param("date") LocalDate date);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.businessId = :businessId " +
           "AND b.status = 'DELIVERED' AND DATE(b.createdAt) = :date")
    java.math.BigDecimal getRevenueByDate(@Param("businessId") Long businessId, @Param("date") LocalDate date);

    boolean existsByIdAndBusinessId(Long bookingId, Long businessId);

    boolean existsByIdAndUserId(Long bookingId, Long userId);
}
