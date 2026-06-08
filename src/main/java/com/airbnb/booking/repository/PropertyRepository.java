package com.airbnb.booking.repository;

import com.airbnb.booking.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Property p
        WHERE (:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%')))
          AND (:minPrice IS NULL OR p.pricePerNight >= :minPrice)
          AND (:maxPrice IS NULL OR p.pricePerNight <= :maxPrice)
          AND p.id IN (
              SELECT pa.property.id FROM PropertyAvailability pa
              WHERE pa.availableFrom <= :startDate AND pa.availableTo >= :endDate
          )
          AND p.id NOT IN (
              SELECT b.property.id FROM Booking b
              WHERE b.status IN ('CONFIRMED', 'REQUESTED')
                AND b.startDate < :endDate AND b.endDate > :startDate
          )
    """)
    Page<Property> searchAvailableProperties(
            @Param("location") String location,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM Property p
        JOIN p.bookings b
        WHERE b.status = 'COMPLETED'
        GROUP BY p
        ORDER BY COUNT(b) DESC
    """)
    List<Property> findPopularProperties(Pageable pageable);

    List<Property> findByHostId(Long hostId);
}
