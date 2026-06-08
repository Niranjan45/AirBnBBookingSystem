package com.airbnb.booking.service.impl;

import com.airbnb.booking.dto.request.BookingRequest;
import com.airbnb.booking.dto.response.BookingResponse;
import com.airbnb.booking.dto.response.BookingStatsResponse;
import com.airbnb.booking.entity.Booking;
import com.airbnb.booking.entity.Property;
import com.airbnb.booking.entity.User;
import com.airbnb.booking.enums.BookingStatus;
import com.airbnb.booking.exception.BadRequestException;
import com.airbnb.booking.exception.BookingConflictException;
import com.airbnb.booking.exception.ResourceNotFoundException;
import com.airbnb.booking.exception.UnauthorizedException;
import com.airbnb.booking.repository.BookingRepository;
import com.airbnb.booking.repository.PropertyRepository;
import com.airbnb.booking.repository.UserRepository;
import com.airbnb.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String guestEmail) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        User guest = getUserByEmail(guestEmail);
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

        // Prevent host from booking their own property
        if (property.getHost().getId().equals(guest.getId())) {
            throw new BadRequestException("Hosts cannot book their own properties");
        }

        // Check for overlapping bookings — prevents double booking
        boolean hasConflict = bookingRepository.existsOverlappingBooking(
                property.getId(), request.getStartDate(), request.getEndDate());
        if (hasConflict) {
            throw new BookingConflictException(
                    "Property is already booked for the selected dates: "
                    + request.getStartDate() + " to " + request.getEndDate());
        }

        long nights = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        BigDecimal totalPrice = property.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = Booking.builder()
                .property(property)
                .guest(guest)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking created: {} for property: {} by guest: {}",
                booking.getId(), property.getId(), guestEmail);

        return toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        boolean isGuest = booking.getGuest().getEmail().equals(userEmail);
        boolean isHost = booking.getProperty().getHost().getEmail().equals(userEmail);

        if (!isGuest && !isHost) {
            throw new UnauthorizedException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Completed bookings cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);
        log.info("Booking cancelled: {}", bookingId);
        return toResponse(booking);
    }

    @Override
    public List<BookingResponse> getUserBookings(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return bookingRepository.findByGuestId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getHostBookings(String hostEmail) {
        User host = getUserByEmail(hostEmail);
        return bookingRepository.findByPropertyHostId(host.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getPropertyBookings(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property", propertyId);
        }
        return bookingRepository.findByPropertyId(propertyId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public BookingStatsResponse getBookingStats(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        List<Object[]> rawStats = bookingRepository.getBookingStatsByProperty(propertyId);
        Map<String, Long> statsByStatus = new HashMap<>();
        long total = 0;

        for (Object[] row : rawStats) {
            String status = row[0].toString();
            Long count = (Long) row[1];
            statsByStatus.put(status, count);
            total += count;
        }

        return BookingStatsResponse.builder()
                .propertyId(propertyId)
                .propertyTitle(property.getTitle())
                .bookingsByStatus(statsByStatus)
                .totalBookings(total)
                .build();
    }

    // ---- helpers ----

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private BookingResponse toResponse(Booking booking) {
        long nights = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        return BookingResponse.builder()
                .id(booking.getId())
                .propertyId(booking.getProperty().getId())
                .propertyTitle(booking.getProperty().getTitle())
                .propertyLocation(booking.getProperty().getLocation())
                .guestId(booking.getGuest().getId())
                .guestName(booking.getGuest().getName())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .numberOfNights(nights)
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
