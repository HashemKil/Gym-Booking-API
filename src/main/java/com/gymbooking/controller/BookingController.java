package com.gymbooking.controller;

import com.gymbooking.dto.BookingRequest;
import com.gymbooking.dto.BookingResponse;
import com.gymbooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Book members into classes and cancel bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Book a member into a class")
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody BookingRequest request) {
        BookingResponse created = bookingService.book(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a booking")
    public BookingResponse findById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking and free the spot")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
