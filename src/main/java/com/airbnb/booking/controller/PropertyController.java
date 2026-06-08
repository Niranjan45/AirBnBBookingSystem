package com.airbnb.booking.controller;

import com.airbnb.booking.dto.request.AvailabilityRequest;
import com.airbnb.booking.dto.request.PropertyRequest;
import com.airbnb.booking.dto.response.ApiResponse;
import com.airbnb.booking.dto.response.AvailabilityResponse;
import com.airbnb.booking.dto.response.PropertyResponse;
import com.airbnb.booking.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Property listing and search operations")
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @Operation(summary = "Create a new property listing (HOST only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PropertyResponse property = propertyService.createProperty(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property created successfully", property));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a property (HOST only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PropertyResponse property = propertyService.updateProperty(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully", property));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a property (HOST only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        propertyService.deleteProperty(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Property deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property details by ID")
    public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Property fetched", propertyService.getPropertyById(id)));
    }

    @GetMapping
    @Operation(summary = "Search properties by location, dates, and price range")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> searchProperties(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "pricePerNight") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Page<PropertyResponse> properties = propertyService.searchProperties(
                location, startDate, endDate, minPrice, maxPrice,
                PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Properties fetched", properties));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get most popular properties (by completed bookings)")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPopularProperties(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Popular properties fetched",
                propertyService.getPopularProperties(limit)));
    }

    @GetMapping("/my-listings")
    @Operation(summary = "Get all properties listed by the authenticated host",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getMyListings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Your listings fetched",
                propertyService.getHostProperties(userDetails.getUsername())));
    }

    @PostMapping("/{id}/availability")
    @Operation(summary = "Set availability dates for a property (HOST only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AvailabilityResponse>> setAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AvailabilityResponse availability = propertyService.setAvailability(
                id, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Availability set successfully", availability));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get availability dates for a property")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getAvailability(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Availability fetched",
                propertyService.getAvailability(id)));
    }
}
