package com.airbnb.booking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class BookingStatsResponse {
    private Long propertyId;
    private String propertyTitle;
    private Map<String, Long> bookingsByStatus;
    private Long totalBookings;
}
