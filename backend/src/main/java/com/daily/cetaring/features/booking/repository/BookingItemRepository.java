package com.daily.cetaring.features.booking.repository;

import com.daily.cetaring.features.booking.entity.BookingItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

    List<BookingItem> findByBooking_Id(Long bookingId);

    @Query("SELECT bi FROM BookingItem bi WHERE bi.booking.id = :bookingId " +
           "ORDER BY bi.createdAt ASC")
    List<BookingItem> findBookingItemsWithDetails(@Param("bookingId") Long bookingId);

    @Modifying
    @Query("DELETE FROM BookingItem bi WHERE bi.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);

    boolean existsByBooking_Id(Long bookingId);
}
