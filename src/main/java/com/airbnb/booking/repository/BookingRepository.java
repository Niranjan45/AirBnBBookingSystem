package com.airbnb.booking.repository;

import com.airbnb.booking.entity.Booking;
import com.airbnb.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByPropertyId(Long propertyId);

    List<Booking> findByPropertyHostId(Long hostId);

    // Check for overlapping bookings to prevent double-booking
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.property.id = :propertyId
          AND b.status IN ('CONFIRMED', 'REQUESTED')
          AND b.startDate < :endDate
          AND b.endDate > :startDate
    """)
    boolean existsOverlappingBooking(
            @Param("propertyId") Long propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Booking stats per property
    @Query("""
        SELECT b.status, COUNT(b) FROM Booking b
        WHERE b.property.id = :propertyId
        GROUP BY b.status
    """)
    List<Object[]> getBookingStatsByProperty(@Param("propertyId") Long propertyId);

    // Check if guest completed a stay (for review eligibility)
    Optional<Booking> findByPropertyIdAndGuestIdAndStatus(
            Long propertyId, Long guestId, BookingStatus status);
}
