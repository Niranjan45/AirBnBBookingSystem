package com.airbnb.booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailabilityRequest {

    @NotNull(message = "Available from date is required")
    @FutureOrPresent(message = "Available from must be today or in the future")
    private LocalDate availableFrom;

    @NotNull(message = "Available to date is required")
    private LocalDate availableTo;
}
