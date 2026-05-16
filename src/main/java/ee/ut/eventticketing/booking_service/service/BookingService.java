package ee.ut.eventticketing.booking_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.ut.eventticketing.booking_service.client.PaymentClient;
import ee.ut.eventticketing.booking_service.client.TicketingClient;
import ee.ut.eventticketing.booking_service.dto.BookingItemRequest;
import ee.ut.eventticketing.booking_service.dto.BookingItemResponse;
import ee.ut.eventticketing.booking_service.dto.BookingResponse;
import ee.ut.eventticketing.booking_service.dto.CreateBookingRequest;
import ee.ut.eventticketing.booking_service.dto.PaymentInitiationResponse;
import ee.ut.eventticketing.booking_service.exception.BookingNotFoundException;
import ee.ut.eventticketing.booking_service.messaging.BookingEventPublisher;
import ee.ut.eventticketing.booking_service.model.Booking;
import ee.ut.eventticketing.booking_service.model.BookingItem;
import ee.ut.eventticketing.booking_service.model.BookingStatus;
import ee.ut.eventticketing.booking_service.model.Money;
import ee.ut.eventticketing.booking_service.repository.BookingRepository;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketingClient ticketingClient;
    private final PaymentClient paymentClient;
    private final BookingEventPublisher bookingEventPublisher;
    private final TicketCatalogService ticketCatalogService;

    public BookingService(
            BookingRepository bookingRepository,
            TicketingClient ticketingClient,
            PaymentClient paymentClient,
            BookingEventPublisher bookingEventPublisher,
            TicketCatalogService ticketCatalogService) {
        this.bookingRepository = bookingRepository;
        this.ticketingClient = ticketingClient;
        this.paymentClient = paymentClient;
        this.bookingEventPublisher = bookingEventPublisher;
        this.ticketCatalogService = ticketCatalogService;
    }

    public BookingResponse createBooking(CreateBookingRequest request) {
        request.items().forEach(item -> ticketingClient.reserveTickets(item.ticketTypeId(), item.quantity()));

        List<BookingItem> items = request.items().stream()
                .map(item -> toBookingItem(item, request.currency(), request.eventId()))
                .toList();

        Booking booking = new Booking(request.customerId(), request.eventId(), request.currency(), items);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        return toResponse(findBooking(bookingId));
    }

    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = findBooking(bookingId);
        booking.cancel();
        booking.getItems().forEach(item -> ticketingClient.releaseTickets(item.getTicketTypeId(), item.getQuantity()));
        Booking saved = bookingRepository.save(booking);
        bookingEventPublisher.publishBookingCancelled(saved);
        return toResponse(saved);
    }

    public PaymentInitiationResponse initiatePayment(Long bookingId) {
        Booking booking = findBooking(bookingId);
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be paid");
        }
        if (booking.isExpired()) {
            throw new IllegalStateException("Booking reservation has expired");
        }

        Long paymentId = paymentClient.createPayment(
                booking.getBookingId(),
                booking.getTotalAmount().getAmount(),
                booking.getTotalAmount().getCurrency());

        return new PaymentInitiationResponse(
                booking.getBookingId(),
                paymentId,
                "Payment initiated for booking " + booking.getBookingId());
    }

    public BookingResponse confirmBooking(Long bookingId) {
        return toResponse(confirmAndPublish(bookingId));
    }

    public void confirmBookingFromPayment(Long bookingId, Long paymentId) {
        Booking booking = findBooking(bookingId);
        if (booking.getBookingStatus() != BookingStatus.PENDING || booking.isExpired()) {
            return;
        }
        confirmAndPublish(booking);
    }

    private Booking confirmAndPublish(Long bookingId) {
        Booking booking = findBooking(bookingId);
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            return booking;
        }
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }
        if (booking.isExpired()) {
            throw new IllegalStateException("Booking reservation has expired");
        }

        return confirmAndPublish(booking);
    }

    private Booking confirmAndPublish(Booking booking) {
        booking.confirm();
        Booking saved = bookingRepository.save(booking);
        bookingEventPublisher.publishBookingConfirmed(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(Long userId) {
        return bookingRepository.findByCustomerId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    private BookingItem toBookingItem(BookingItemRequest request, String currency, Long eventId) {
        var ticketType = ticketCatalogService.getTicketType(request.ticketTypeId());
        if (!ticketType.eventId().equals(eventId)) {
            throw new IllegalArgumentException("Ticket type does not belong to the selected event");
        }
        if (!ticketType.currency().equalsIgnoreCase(currency)) {
            throw new IllegalArgumentException("Ticket currency does not match booking currency");
        }
        return new BookingItem(
                request.ticketTypeId(),
                request.quantity(),
                new Money(ticketType.price(), ticketType.currency()));
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getBookingId(),
                booking.getCustomerId(),
                booking.getEventId(),
                booking.getBookingStatus(),
                booking.getReservationTime(),
                booking.getReservationExpiry(),
                booking.getTotalAmount().getAmount(),
                booking.getTotalAmount().getCurrency(),
                booking.getItems().stream().map(this::toItemResponse).toList());
    }

    private BookingItemResponse toItemResponse(BookingItem item) {
        return new BookingItemResponse(
                item.getBookingItemId(),
                item.getTicketTypeId(),
                item.getQuantity(),
                item.getUnitPrice().getAmount(),
                item.getUnitPrice().getCurrency());
    }
}
