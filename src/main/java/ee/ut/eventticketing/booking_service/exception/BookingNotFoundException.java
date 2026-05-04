package ee.ut.eventticketing.booking_service.exception;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long bookingId) {
        super("Booking not found with id " + bookingId);
    }
}
