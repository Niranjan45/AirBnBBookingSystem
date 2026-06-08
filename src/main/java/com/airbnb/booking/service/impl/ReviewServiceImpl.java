package com.airbnb.booking.service.impl;

import com.airbnb.booking.dto.request.ReviewRequest;
import com.airbnb.booking.dto.response.ReviewResponse;
import com.airbnb.booking.entity.Property;
import com.airbnb.booking.entity.Review;
import com.airbnb.booking.entity.User;
import com.airbnb.booking.enums.BookingStatus;
import com.airbnb.booking.exception.BadRequestException;
import com.airbnb.booking.exception.DuplicateResourceException;
import com.airbnb.booking.exception.ResourceNotFoundException;
import com.airbnb.booking.repository.BookingRepository;
import com.airbnb.booking.repository.PropertyRepository;
import com.airbnb.booking.repository.ReviewRepository;
import com.airbnb.booking.repository.UserRepository;
import com.airbnb.booking.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse addReview(ReviewRequest request, String guestEmail) {
        User guest = userRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + guestEmail));
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

        // Ensure the guest has completed a stay at this property
        boolean hasCompletedStay = bookingRepository
                .findByPropertyIdAndGuestIdAndStatus(
                        property.getId(), guest.getId(), BookingStatus.COMPLETED)
                .isPresent();

        if (!hasCompletedStay) {
            throw new BadRequestException(
                    "You can only review a property after completing a stay");
        }

        // Prevent duplicate reviews
        if (reviewRepository.existsByPropertyIdAndGuestId(property.getId(), guest.getId())) {
            throw new DuplicateResourceException("You have already reviewed this property");
        }

        Review review = Review.builder()
                .property(property)
                .guest(guest)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);
        log.info("Review added for property: {} by guest: {}", property.getId(), guestEmail);

        return toResponse(review);
    }

    @Override
    public List<ReviewResponse> getPropertyReviews(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property", propertyId);
        }
        return reviewRepository.findByPropertyId(propertyId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .propertyId(review.getProperty().getId())
                .guestId(review.getGuest().getId())
                .guestName(review.getGuest().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
