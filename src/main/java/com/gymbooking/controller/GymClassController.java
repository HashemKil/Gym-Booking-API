package com.gymbooking.controller;

import com.gymbooking.dto.BookingResponse;
import com.gymbooking.dto.GymClassRequest;
import com.gymbooking.dto.GymClassResponse;
import com.gymbooking.service.GymClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/classes")
@Tag(name = "Classes", description = "Manage the gym class schedule")
public class GymClassController {

    private final GymClassService gymClassService;

    public GymClassController(GymClassService gymClassService) {
        this.gymClassService = gymClassService;
    }

    @PostMapping
    @Operation(summary = "Schedule a new class")
    public ResponseEntity<GymClassResponse> create(@Valid @RequestBody GymClassRequest request) {
        GymClassResponse created = gymClassService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List all classes ordered by start time")
    public List<GymClassResponse> findAll() {
        return gymClassService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a class with its remaining spots")
    public GymClassResponse findById(@PathVariable Long id) {
        return gymClassService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a class")
    public GymClassResponse update(@PathVariable Long id, @Valid @RequestBody GymClassRequest request) {
        return gymClassService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a class with no active bookings")
    public void delete(@PathVariable Long id) {
        gymClassService.delete(id);
    }

    @GetMapping("/{id}/bookings")
    @Operation(summary = "List bookings for a class")
    public List<BookingResponse> findBookings(@PathVariable Long id) {
        return gymClassService.findBookings(id);
    }
}
