package ee.ut.eventticketing.booking_service.dto;

public record PaymentInitiationResponse(
        Long bookingId,
        Long paymentId,
        String message) {
}
