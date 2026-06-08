package com.airbnb.booking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AvailabilityResponse {
    private Long id;
    private LocalDate availableFrom;
    private LocalDate availableTo;
}
