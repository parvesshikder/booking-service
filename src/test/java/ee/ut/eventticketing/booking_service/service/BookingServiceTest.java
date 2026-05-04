package ee.ut.eventticketing.booking_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.ut.eventticketing.booking_service.client.PaymentClient;
import ee.ut.eventticketing.booking_service.client.TicketingClient;
import ee.ut.eventticketing.booking_service.dto.BookingItemRequest;
import ee.ut.eventticketing.booking_service.dto.BookingResponse;
import ee.ut.eventticketing.booking_service.dto.CreateBookingRequest;
import ee.ut.eventticketing.booking_service.model.Booking;
import ee.ut.eventticketing.booking_service.model.BookingStatus;
import ee.ut.eventticketing.booking_service.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketingClient ticketingClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBookingReservesTicketsAndSavesPendingBooking() {
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                10L,
                "EUR",
                List.of(new BookingItemRequest(5L, 2, BigDecimal.valueOf(25.00))));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.createBooking(request);

        verify(ticketingClient).reserveTickets(5L, 2);
        verify(bookingRepository).save(any(Booking.class));
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(response.items()).hasSize(1);
    }
}
