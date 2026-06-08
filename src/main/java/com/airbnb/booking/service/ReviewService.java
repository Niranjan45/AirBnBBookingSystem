package com.airbnb.booking.service;

import com.airbnb.booking.dto.request.ReviewRequest;
import com.airbnb.booking.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse addReview(ReviewRequest request, String guestEmail);
    List<ReviewResponse> getPropertyReviews(Long propertyId);
}
