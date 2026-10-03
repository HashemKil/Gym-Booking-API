package com.gymbooking.controller;

import com.gymbooking.dto.BookingResponse;
import com.gymbooking.dto.MemberRequest;
import com.gymbooking.dto.MemberResponse;
import com.gymbooking.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Register and look up gym members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @Operation(summary = "Register a new member")
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody MemberRequest request) {
        MemberResponse created = memberService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List all members")
    public List<MemberResponse> findAll() {
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a member")
    public MemberResponse findById(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @GetMapping("/{id}/bookings")
    @Operation(summary = "List a member's bookings, newest first")
    public List<BookingResponse> findBookings(@PathVariable Long id) {
        return memberService.findBookings(id);
    }
}
