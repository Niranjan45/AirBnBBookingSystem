package com.airbnb.booking.repository;

import com.airbnb.booking.entity.PropertyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyAvailabilityRepository extends JpaRepository<PropertyAvailability, Long> {
    List<PropertyAvailability> findByPropertyId(Long propertyId);
    void deleteByPropertyId(Long propertyId);
}
