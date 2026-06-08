package com.airbnb.booking.service.impl;

import com.airbnb.booking.dto.request.AvailabilityRequest;
import com.airbnb.booking.dto.request.PropertyRequest;
import com.airbnb.booking.dto.response.AvailabilityResponse;
import com.airbnb.booking.dto.response.PropertyResponse;
import com.airbnb.booking.entity.Property;
import com.airbnb.booking.entity.PropertyAvailability;
import com.airbnb.booking.entity.User;
import com.airbnb.booking.exception.BadRequestException;
import com.airbnb.booking.exception.ResourceNotFoundException;
import com.airbnb.booking.exception.UnauthorizedException;
import com.airbnb.booking.repository.PropertyAvailabilityRepository;
import com.airbnb.booking.repository.PropertyRepository;
import com.airbnb.booking.repository.ReviewRepository;
import com.airbnb.booking.repository.UserRepository;
import com.airbnb.booking.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public PropertyResponse createProperty(PropertyRequest request, String hostEmail) {
        User host = getUserByEmail(hostEmail);
        Property property = Property.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .pricePerNight(request.getPricePerNight())
                .host(host)
                .build();
        property = propertyRepository.save(property);
        log.info("Property created: {} by host: {}", property.getId(), hostEmail);
        return toResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse updateProperty(Long id, PropertyRequest request, String hostEmail) {
        Property property = getPropertyAndVerifyHost(id, hostEmail);
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setLocation(request.getLocation());
        property.setPricePerNight(request.getPricePerNight());
        property = propertyRepository.save(property);
        log.info("Property updated: {}", id);
        return toResponse(property);
    }

    @Override
    public PropertyResponse getPropertyById(Long id) {
        return toResponse(propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id)));
    }

    @Override
    public Page<PropertyResponse> searchProperties(String location, LocalDate startDate,
                                                    LocalDate endDate, BigDecimal minPrice,
                                                    BigDecimal maxPrice, Pageable pageable) {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new BadRequestException("End date must be after start date");
        }

        if (startDate != null && endDate != null) {
            return propertyRepository
                    .searchAvailableProperties(location, startDate, endDate, minPrice, maxPrice, pageable)
                    .map(this::toResponse);
        }

        return propertyRepository
                .findByLocationContainingIgnoreCase(location != null ? location : "", pageable)
                .map(this::toResponse);
    }

    @Override
    public List<PropertyResponse> getPopularProperties(int limit) {
        return propertyRepository.findPopularProperties(PageRequest.of(0, limit))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PropertyResponse> getHostProperties(String hostEmail) {
        User host = getUserByEmail(hostEmail);
        return propertyRepository.findByHostId(host.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AvailabilityResponse setAvailability(Long propertyId, AvailabilityRequest request,
                                                 String hostEmail) {
        Property property = getPropertyAndVerifyHost(propertyId, hostEmail);

        if (!request.getAvailableTo().isAfter(request.getAvailableFrom())) {
            throw new BadRequestException("Available to date must be after available from date");
        }

        PropertyAvailability availability = PropertyAvailability.builder()
                .property(property)
                .availableFrom(request.getAvailableFrom())
                .availableTo(request.getAvailableTo())
                .build();

        availability = availabilityRepository.save(availability);
        log.info("Availability set for property: {}", propertyId);

        return AvailabilityResponse.builder()
                .id(availability.getId())
                .availableFrom(availability.getAvailableFrom())
                .availableTo(availability.getAvailableTo())
                .build();
    }

    @Override
    public List<AvailabilityResponse> getAvailability(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property", propertyId);
        }
        return availabilityRepository.findByPropertyId(propertyId)
                .stream()
                .map(a -> AvailabilityResponse.builder()
                        .id(a.getId())
                        .availableFrom(a.getAvailableFrom())
                        .availableTo(a.getAvailableTo())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProperty(Long id, String hostEmail) {
        getPropertyAndVerifyHost(id, hostEmail);
        propertyRepository.deleteById(id);
        log.info("Property deleted: {}", id);
    }

    // ---- helpers ----

    private Property getPropertyAndVerifyHost(Long propertyId, String hostEmail) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));
        if (!property.getHost().getEmail().equals(hostEmail)) {
            throw new UnauthorizedException("You are not the host of this property");
        }
        return property;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private PropertyResponse toResponse(Property property) {
        Double avgRating = reviewRepository.findAverageRatingByPropertyId(property.getId());
        int totalReviews = property.getReviews() != null ? property.getReviews().size() : 0;

        List<AvailabilityResponse> availabilities = availabilityRepository
                .findByPropertyId(property.getId())
                .stream()
                .map(a -> AvailabilityResponse.builder()
                        .id(a.getId())
                        .availableFrom(a.getAvailableFrom())
                        .availableTo(a.getAvailableTo())
                        .build())
                .collect(Collectors.toList());

        return PropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .location(property.getLocation())
                .pricePerNight(property.getPricePerNight())
                .hostId(property.getHost().getId())
                .hostName(property.getHost().getName())
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews)
                .availabilities(availabilities)
                .build();
    }
}
