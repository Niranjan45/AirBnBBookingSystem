package com.airbnb.booking;

import com.airbnb.booking.dto.request.BookingRequest;
import com.airbnb.booking.dto.response.BookingResponse;
import com.airbnb.booking.entity.Booking;
import com.airbnb.booking.entity.Property;
import com.airbnb.booking.entity.User;
import com.airbnb.booking.enums.BookingStatus;
import com.airbnb.booking.enums.Role;
import com.airbnb.booking.exception.BookingConflictException;
import com.airbnb.booking.repository.BookingRepository;
import com.airbnb.booking.repository.PropertyRepository;
import com.airbnb.booking.repository.UserRepository;
import com.airbnb.booking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BookingServiceImpl bookingService;

    private User guest;
    private User host;
    private Property property;

    @BeforeEach
    void setUp() {
        host = User.builder().id(1L).name("Host User")
                .email("host@test.com").role(Role.HOST).build();

        guest = User.builder().id(2L).name("Guest User")
                .email("guest@test.com").role(Role.GUEST).build();

        property = Property.builder().id(1L).title("Test Property")
                .location("Hyderabad").pricePerNight(BigDecimal.valueOf(1500))
                .host(host).build();
    }

    @Test
    void createBooking_Success() {
        BookingRequest request = new BookingRequest();
        request.setPropertyId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(4));

        when(userRepository.findByEmail("guest@test.com")).thenReturn(Optional.of(guest));
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(bookingRepository.existsOverlappingBooking(anyLong(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b = Booking.builder()
                    .id(10L).property(property).guest(guest)
                    .startDate(b.getStartDate()).endDate(b.getEndDate())
                    .totalPrice(b.getTotalPrice()).status(BookingStatus.CONFIRMED).build();
            return b;
        });

        BookingResponse response = bookingService.createBooking(request, "guest@test.com");

        assertNotNull(response);
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertEquals(BigDecimal.valueOf(4500), response.getTotalPrice());
        assertEquals(3L, response.getNumberOfNights());
    }

    @Test
    void createBooking_OverlapConflict_Throws() {
        BookingRequest request = new BookingRequest();
        request.setPropertyId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        when(userRepository.findByEmail("guest@test.com")).thenReturn(Optional.of(guest));
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(bookingRepository.existsOverlappingBooking(anyLong(), any(), any())).thenReturn(true);

        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(request, "guest@test.com"));
    }

    @Test
    void cancelBooking_Success() {
        Booking booking = Booking.builder()
                .id(1L).property(property).guest(guest)
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .totalPrice(BigDecimal.valueOf(4500))
                .status(BookingStatus.CONFIRMED).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.cancelBooking(1L, "guest@test.com");

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
    }
}
