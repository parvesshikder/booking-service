package ee.ut.eventticketing.booking_service.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    private Long customerId;

    private Long eventId;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    private LocalDateTime reservationTime;

    private LocalDateTime reservationExpiry;

    @Embedded
    private Money totalAmount;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingItem> items = new ArrayList<>();

    protected Booking() {
    }

    public Booking(Long customerId, Long eventId, String currency, List<BookingItem> items) {
        this.customerId = customerId;
        this.eventId = eventId;
        this.bookingStatus = BookingStatus.PENDING;
        this.reservationTime = LocalDateTime.now();
        this.reservationExpiry = reservationTime.plusMinutes(15);
        items.forEach(this::addItem);
        this.totalAmount = calculateTotal(currency);
    }

    public void addItem(BookingItem item) {
        item.assignToBooking(this);
        this.items.add(item);
    }

    public void cancel() {
        if (bookingStatus == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Confirmed bookings cannot be cancelled here");
        }
        this.bookingStatus = BookingStatus.CANCELLED;
    }

    public void confirm() {
        if (bookingStatus != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(reservationExpiry);
    }

    private Money calculateTotal(String currency) {
        BigDecimal total = items.stream()
            .map(item -> item.getUnitPrice().getAmount().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Money(total, currency);
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getEventId() {
        return eventId;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public LocalDateTime getReservationExpiry() {
        return reservationExpiry;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public List<BookingItem> getItems() {
        return items;
    }
}