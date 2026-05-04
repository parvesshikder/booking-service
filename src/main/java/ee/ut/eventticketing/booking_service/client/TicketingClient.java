package ee.ut.eventticketing.booking_service.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TicketingClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ticketingServiceBaseUrl;
    private final boolean mockEnabled;

    public TicketingClient(
            @Value("${services.ticketing.base-url}") String ticketingServiceBaseUrl,
            @Value("${services.ticketing.mock-enabled:true}") boolean mockEnabled) {
        this.ticketingServiceBaseUrl = ticketingServiceBaseUrl;
        this.mockEnabled = mockEnabled;
    }

    public void reserveTickets(Long ticketTypeId, int quantity) {
        if (mockEnabled) {
            return;
        }

        Map<String, Object> request = Map.of("quantity", quantity);

        restTemplate.exchange(
                ticketingServiceBaseUrl + "/ticket-types/{id}/reserve",
                HttpMethod.PATCH,
                new HttpEntity<>(request),
                Void.class,
                ticketTypeId);
    }

    public void releaseTickets(Long ticketTypeId, int quantity) {
        if (mockEnabled) {
            return;
        }

        Map<String, Object> request = Map.of("quantity", quantity);

        restTemplate.exchange(
                ticketingServiceBaseUrl + "/ticket-types/{id}/release",
                HttpMethod.PATCH,
                new HttpEntity<>(request),
                Void.class,
                ticketTypeId);
    }
}
