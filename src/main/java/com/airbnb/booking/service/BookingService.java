package com.airbnb.booking.service;

import com.airbnb.booking.dto.request.BookingRequest;
import com.airbnb.booking.dto.response.BookingResponse;
import com.airbnb.booking.dto.response.BookingStatsResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, String guestEmail);
    BookingResponse cancelBooking(Long bookingId, String userEmail);
    List<BookingResponse> getUserBookings(Long userId);
    List<BookingResponse> getHostBookings(String hostEmail);
    List<BookingResponse> getPropertyBookings(Long propertyId);
    BookingStatsResponse getBookingStats(Long propertyId);
}
