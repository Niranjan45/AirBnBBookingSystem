package com.airbnb.booking.dto.response;

import com.airbnb.booking.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private String propertyLocation;
    private Long guestId;
    private String guestName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long numberOfNights;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
