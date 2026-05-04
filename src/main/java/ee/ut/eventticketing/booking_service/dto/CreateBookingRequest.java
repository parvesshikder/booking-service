package ee.ut.eventticketing.booking_service.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull Long customerId,
        @NotNull Long eventId,
        @NotBlank String currency,
        @NotEmpty List<@Valid BookingItemRequest> items) {
}
