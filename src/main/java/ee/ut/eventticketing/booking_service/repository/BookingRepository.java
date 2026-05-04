package ee.ut.eventticketing.booking_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ee.ut.eventticketing.booking_service.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(Long customerId);
}