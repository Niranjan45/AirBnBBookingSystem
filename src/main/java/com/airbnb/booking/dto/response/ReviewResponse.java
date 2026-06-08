package com.airbnb.booking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long propertyId;
    private Long guestId;
    private String guestName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
