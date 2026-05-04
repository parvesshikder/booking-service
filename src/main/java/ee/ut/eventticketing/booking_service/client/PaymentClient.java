package ee.ut.eventticketing.booking_service.client;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String paymentServiceBaseUrl;
    private final boolean mockEnabled;

    public PaymentClient(
            @Value("${services.payment.base-url}") String paymentServiceBaseUrl,
            @Value("${services.payment.mock-enabled:true}") boolean mockEnabled) {
        this.paymentServiceBaseUrl = paymentServiceBaseUrl;
        this.mockEnabled = mockEnabled;
    }

    public Long createPayment(Long bookingId, BigDecimal amount, String currency) {
        if (mockEnabled) {
            return bookingId;
        }

        Map<String, Object> request = Map.of(
                "bookingId", bookingId,
                "amount", amount,
                "currency", currency,
                "paymentMethod", "CARD");

        PaymentResponse response = restTemplate.postForObject(
                paymentServiceBaseUrl + "/payments",
                new HttpEntity<>(request),
                PaymentResponse.class);

        if (response == null || response.paymentId() == null) {
            throw new IllegalStateException("Payment service did not return a payment id");
        }

        return response.paymentId();
    }

    private record PaymentResponse(Long paymentId) {
    }
}
