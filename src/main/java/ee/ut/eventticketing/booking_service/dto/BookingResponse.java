package ee.ut.eventticketing.booking_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ee.ut.eventticketing.booking_service.model.BookingStatus;

public record BookingResponse(
        Long bookingId,
        Long customerId,
        Long eventId,
        BookingStatus status,
        LocalDateTime reservationTime,
        LocalDateTime reservationExpiry,
        BigDecimal totalAmount,
        String currency,
        List<BookingItemResponse> items) {
}
