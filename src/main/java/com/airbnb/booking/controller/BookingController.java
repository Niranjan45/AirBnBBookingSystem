package com.airbnb.booking.controller;

import com.airbnb.booking.dto.request.BookingRequest;
import com.airbnb.booking.dto.response.ApiResponse;
import com.airbnb.booking.dto.response.BookingResponse;
import com.airbnb.booking.dto.response.BookingStatsResponse;
import com.airbnb.booking.service.BookingService;
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
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a booking request (GUEST only)")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        BookingResponse booking = bookingService.createBooking(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", booking));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking (GUEST or HOST)")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        BookingResponse booking = bookingService.cancelBooking(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", booking));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all bookings for a specific guest")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUserBookings(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User bookings fetched",
                bookingService.getUserBookings(userId)));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Get all bookings made by the authenticated guest")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        // Resolve via host bookings or guest bookings depending on role;
        // here we delegate to the service which filters by email
        return ResponseEntity.ok(ApiResponse.success("Your bookings fetched",
                bookingService.getHostBookings(userDetails.getUsername())));
    }

    @GetMapping("/host")
    @Operation(summary = "Get all bookings for the authenticated host's properties")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getHostBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Host bookings fetched",
                bookingService.getHostBookings(userDetails.getUsername())));
    }

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get all bookings for a specific property")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getPropertyBookings(
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(ApiResponse.success("Property bookings fetched",
                bookingService.getPropertyBookings(propertyId)));
    }

    @GetMapping("/property/{propertyId}/stats")
    @Operation(summary = "Get booking statistics for a property")
    public ResponseEntity<ApiResponse<BookingStatsResponse>> getBookingStats(
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(ApiResponse.success("Booking stats fetched",
                bookingService.getBookingStats(propertyId)));
    }
}
