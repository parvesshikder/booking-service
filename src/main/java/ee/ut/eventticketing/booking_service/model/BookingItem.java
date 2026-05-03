package ee.ut.eventticketing.booking_service.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingItemId;

    private Long ticketTypeId;

    private int quantity;

    @Embedded
    private Money unitPrice;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    protected BookingItem() {
    }

    public BookingItem(Long ticketTypeId, int quantity, Money unitPrice) {
        this.ticketTypeId = ticketTypeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void assignToBooking(Booking booking) {
        this.booking = booking;
    }

    public Long getBookingItemId() {
        return bookingItemId;
    }

    public Long getTicketTypeId() {
        return ticketTypeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }
}