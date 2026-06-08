package com.airbnb.booking.controller;

import com.airbnb.booking.dto.request.ReviewRequest;
import com.airbnb.booking.dto.response.ApiResponse;
import com.airbnb.booking.dto.response.ReviewResponse;
import com.airbnb.booking.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Property reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Add a review for a completed stay (GUEST only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReviewResponse review = reviewService.addReview(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review added successfully", review));
    }

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get all reviews for a property")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getPropertyReviews(
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(ApiResponse.success("Reviews fetched",
                reviewService.getPropertyReviews(propertyId)));
    }
}
