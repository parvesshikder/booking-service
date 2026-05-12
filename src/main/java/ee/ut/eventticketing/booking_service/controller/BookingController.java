package ee.ut.eventticketing.booking_service.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ee.ut.eventticketing.booking_service.dto.BookingItemRequest;
import ee.ut.eventticketing.booking_service.dto.BookingResponse;
import ee.ut.eventticketing.booking_service.dto.CreateBookingRequest;
import ee.ut.eventticketing.booking_service.dto.PaymentInitiationResponse;
import ee.ut.eventticketing.booking_service.service.BookingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/bookings/{id}")
    public BookingResponse getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @DeleteMapping("/bookings/{id}")
    public BookingResponse cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }

    @PostMapping("/bookings/{id}/pay")
    public PaymentInitiationResponse initiatePayment(@PathVariable Long id) {
        return bookingService.initiatePayment(id);
    }

    @PostMapping("/bookings/{id}/confirm")
    public BookingResponse confirmBooking(@PathVariable Long id) {
        return bookingService.confirmBooking(id);
    }

    @GetMapping("/users/{userId}/bookings")
    public List<BookingResponse> getBookingsByUser(@PathVariable Long userId) {
        return bookingService.getBookingsByUser(userId);
    }

    @GetMapping("/demo/users/{userId}/bookings")
    public List<BookingResponse> getDemoBookingsByUser(@PathVariable Long userId) {
        return bookingService.getBookingsByUser(userId);
    }

    @GetMapping("/demo/bookings")
    public List<BookingResponse> getDemoBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping("/demo/bookings/payment-check")
    public Map<String, Object> createDemoBookingAndPayment() {
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                100L,
                "EUR",
                List.of(new BookingItemRequest(10L, 1, BigDecimal.valueOf(25.00))));

        BookingResponse booking = bookingService.createBooking(request);
        PaymentInitiationResponse payment = bookingService.initiatePayment(booking.bookingId());

        return Map.of(
                "message", "Booking Service created a booking and called Payment Service",
                "bookingId", booking.bookingId(),
                "bookingStatus", booking.status(),
                "paymentId", payment.paymentId(),
                "paymentMessage", payment.message(),
                "totalAmount", booking.totalAmount(),
                "currency", booking.currency());
    }

    @PostMapping("/demo/bookings/book-and-pay")
    public Map<String, Object> createSelectedDemoBookingAndPayment(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse booking = bookingService.createBooking(request);
        PaymentInitiationResponse payment = bookingService.initiatePayment(booking.bookingId());

        return Map.of(
                "message", "Booking Service created a booking and called Payment Service",
                "bookingId", booking.bookingId(),
                "bookingStatus", booking.status(),
                "paymentId", payment.paymentId(),
                "paymentMessage", payment.message(),
                "totalAmount", booking.totalAmount(),
                "currency", booking.currency());
    }
}
