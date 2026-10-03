package com.gymbooking.service;

import com.gymbooking.dto.BookingRequest;
import com.gymbooking.dto.BookingResponse;
import com.gymbooking.entity.Booking;
import com.gymbooking.entity.BookingStatus;
import com.gymbooking.entity.GymClass;
import com.gymbooking.entity.Member;
import com.gymbooking.exception.BookingAlreadyCancelledException;
import com.gymbooking.exception.ClassAlreadyStartedException;
import com.gymbooking.exception.ClassFullException;
import com.gymbooking.exception.DuplicateBookingException;
import com.gymbooking.exception.ResourceNotFoundException;
import com.gymbooking.repository.BookingRepository;
import com.gymbooking.repository.GymClassRepository;
import com.gymbooking.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MemberRepository memberRepository;
    private final GymClassRepository gymClassRepository;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository,
                          MemberRepository memberRepository,
                          GymClassRepository gymClassRepository,
                          Clock clock) {
        this.bookingRepository = bookingRepository;
        this.memberRepository = memberRepository;
        this.gymClassRepository = gymClassRepository;
        this.clock = clock;
    }

    @Transactional
    public BookingResponse book(BookingRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.memberId()));

        GymClass gymClass = gymClassRepository.findByIdForUpdate(request.classId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", request.classId()));

        LocalDateTime now = LocalDateTime.now(clock);

        if (gymClass.hasStarted(now)) {
            throw new ClassAlreadyStartedException(gymClass.getName());
        }

        if (bookingRepository.existsByMemberIdAndGymClassIdAndStatus(
                member.getId(), gymClass.getId(), BookingStatus.ACTIVE)) {
            throw new DuplicateBookingException(member.getFullName(), gymClass.getName());
        }

        long activeBookings = bookingRepository.countByGymClassIdAndStatus(gymClass.getId(), BookingStatus.ACTIVE);
        if (activeBookings >= gymClass.getCapacity()) {
            throw new ClassFullException(gymClass.getName());
        }

        Booking booking = bookingRepository.save(new Booking(member, gymClass, now));
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse cancel(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        LocalDateTime now = LocalDateTime.now(clock);

        if (booking.isCancelled()) {
            throw new BookingAlreadyCancelledException(bookingId);
        }

        if (booking.getGymClass().hasStarted(now)) {
            throw new ClassAlreadyStartedException(booking.getGymClass().getName());
        }

        booking.cancel(now);
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse findById(Long bookingId) {
        return BookingResponse.from(getBookingOrThrow(bookingId));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findWithDetailsById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }
}
