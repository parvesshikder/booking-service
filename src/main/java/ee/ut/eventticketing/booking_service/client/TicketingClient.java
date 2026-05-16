package ee.ut.eventticketing.booking_service.client;

import org.springframework.stereotype.Component;

import ee.ut.eventticketing.booking_service.service.TicketCatalogService;

@Component
public class TicketingClient {

    private final TicketCatalogService ticketCatalogService;

    public TicketingClient(TicketCatalogService ticketCatalogService) {
        this.ticketCatalogService = ticketCatalogService;
    }

    public void reserveTickets(Long ticketTypeId, int quantity) {
        ticketCatalogService.reserveTickets(ticketTypeId, quantity);
    }

    public void releaseTickets(Long ticketTypeId, int quantity) {
        ticketCatalogService.releaseTickets(ticketTypeId, quantity);
    }
}
