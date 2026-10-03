package com.gymbooking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymbooking.repository.BookingRepository;
import com.gymbooking.repository.GymClassRepository;
import com.gymbooking.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GymClassRepository gymClassRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
        gymClassRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void fullClassRejectsBookingUntilASpotIsFreed() throws Exception {
        long classId = createClass("HIIT Blast", 2);
        long first = registerMember("Ahmad Ali", "ahmad@example.com");
        long second = registerMember("Layla Mansour", "layla@example.com");
        long third = registerMember("Yousef Karim", "yousef@example.com");

        long firstBookingId = book(first, classId);
        book(second, classId);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("memberId", third, "classId", classId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Class 'HIIT Blast' is fully booked"));

        mockMvc.perform(patch("/api/bookings/{id}/cancel", firstBookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        book(third, classId);

        mockMvc.perform(get("/api/classes/{id}", classId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookedCount").value(2))
                .andExpect(jsonPath("$.spotsLeft").value(0));
    }

    @Test
    void sameMemberCannotBookTheSameClassTwice() throws Exception {
        long classId = createClass("Morning Yoga", 10);
        long memberId = registerMember("Noor Hamdan", "noor@example.com");

        book(memberId, classId);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("memberId", memberId, "classId", classId))))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidClassRequestReturnsFieldErrors() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "",
                "instructor", "Lina Haddad",
                "startTime", futureTime(),
                "durationMinutes", 5,
                "capacity", 0
        );

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.durationMinutes").exists())
                .andExpect(jsonPath("$.fieldErrors.capacity").exists());
    }

    @Test
    void unknownClassReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/classes/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Class with id 9999 not found"));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        registerMember("Ahmad Ali", "ahmad@example.com");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Another Ahmad", "email", "AHMAD@example.com"))))
                .andExpect(status().isConflict());
    }

    private long createClass(String name, int capacity) throws Exception {
        Map<String, Object> body = Map.of(
                "name", name,
                "instructor", "Omar Saleh",
                "startTime", futureTime(),
                "durationMinutes", 45,
                "capacity", capacity
        );
        return postAndReadId("/api/classes", body);
    }

    private long registerMember(String fullName, String email) throws Exception {
        return postAndReadId("/api/members", Map.of("fullName", fullName, "email", email));
    }

    private long book(long memberId, long classId) throws Exception {
        return postAndReadId("/api/bookings", Map.of("memberId", memberId, "classId", classId));
    }

    private long postAndReadId(String url, Map<String, ?> body) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    private String futureTime() {
        return LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES).toString();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
