package ee.ut.eventticketing.booking_service.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import ee.ut.eventticketing.booking_service.controller.BookingController;
import ee.ut.eventticketing.booking_service.service.BookingService;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void bookingEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isUnauthorized());
    }
}
