package com.airbnb.booking.service;

import com.airbnb.booking.dto.request.AvailabilityRequest;
import com.airbnb.booking.dto.request.PropertyRequest;
import com.airbnb.booking.dto.response.AvailabilityResponse;
import com.airbnb.booking.dto.response.PropertyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PropertyService {
    PropertyResponse createProperty(PropertyRequest request, String hostEmail);
    PropertyResponse updateProperty(Long id, PropertyRequest request, String hostEmail);
    PropertyResponse getPropertyById(Long id);
    Page<PropertyResponse> searchProperties(String location, LocalDate startDate,
                                            LocalDate endDate, BigDecimal minPrice,
                                            BigDecimal maxPrice, Pageable pageable);
    List<PropertyResponse> getPopularProperties(int limit);
    List<PropertyResponse> getHostProperties(String hostEmail);
    AvailabilityResponse setAvailability(Long propertyId, AvailabilityRequest request, String hostEmail);
    List<AvailabilityResponse> getAvailability(Long propertyId);
    void deleteProperty(Long id, String hostEmail);
}
