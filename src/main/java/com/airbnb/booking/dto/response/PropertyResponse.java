package com.airbnb.booking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PropertyResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private BigDecimal pricePerNight;
    private Long hostId;
    private String hostName;
    private Double averageRating;
    private Integer totalReviews;
    private List<AvailabilityResponse> availabilities;
}
