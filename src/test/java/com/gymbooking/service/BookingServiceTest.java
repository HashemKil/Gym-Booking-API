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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-10T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime NOW = LocalDateTime.now(CLOCK);

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GymClassRepository gymClassRepository;

    private BookingService bookingService;
    private Member member;
    private GymClass upcomingClass;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository, memberRepository, gymClassRepository, CLOCK);

        member = new Member("Ahmad Ali", "ahmad@example.com", NOW);
        ReflectionTestUtils.setField(member, "id", 1L);

        upcomingClass = new GymClass("HIIT Blast", "Omar Saleh", NOW.plusDays(1), 45, 2);
        ReflectionTestUtils.setField(upcomingClass, "id", 10L);
    }

    @Test
    void booksMemberWhenSpotsAreAvailable() {
        givenMemberAndClass(upcomingClass);
        when(bookingRepository.existsByMemberIdAndGymClassIdAndStatus(1L, 10L, BookingStatus.ACTIVE)).thenReturn(false);
        when(bookingRepository.countByGymClassIdAndStatus(10L, BookingStatus.ACTIVE)).thenReturn(1L);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.book(new BookingRequest(1L, 10L));

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.classId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(BookingStatus.ACTIVE);
        assertThat(response.bookedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsBookingWhenClassIsFull() {
        givenMemberAndClass(upcomingClass);
        when(bookingRepository.existsByMemberIdAndGymClassIdAndStatus(1L, 10L, BookingStatus.ACTIVE)).thenReturn(false);
        when(bookingRepository.countByGymClassIdAndStatus(10L, BookingStatus.ACTIVE)).thenReturn(2L);

        assertThatThrownBy(() -> bookingService.book(new BookingRequest(1L, 10L)))
                .isInstanceOf(ClassFullException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateBooking() {
        givenMemberAndClass(upcomingClass);
        when(bookingRepository.existsByMemberIdAndGymClassIdAndStatus(1L, 10L, BookingStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.book(new BookingRequest(1L, 10L)))
                .isInstanceOf(DuplicateBookingException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectsBookingForClassThatAlreadyStarted() {
        GymClass startedClass = new GymClass("Pilates Core", "Dana Aziz", NOW.minusMinutes(5), 50, 10);
        ReflectionTestUtils.setField(startedClass, "id", 11L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(gymClassRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(startedClass));

        assertThatThrownBy(() -> bookingService.book(new BookingRequest(1L, 11L)))
                .isInstanceOf(ClassAlreadyStartedException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectsBookingForUnknownMember() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.book(new BookingRequest(99L, 10L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member with id 99");
    }

    @Test
    void cancelMarksBookingAsCancelled() {
        Booking booking = new Booking(member, upcomingClass, NOW.minusDays(1));
        ReflectionTestUtils.setField(booking, "id", 5L);
        when(bookingRepository.findWithDetailsById(5L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.cancel(5L);

        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(response.cancelledAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsCancellingTwice() {
        Booking booking = new Booking(member, upcomingClass, NOW.minusDays(1));
        booking.cancel(NOW.minusHours(1));
        ReflectionTestUtils.setField(booking, "id", 5L);
        when(bookingRepository.findWithDetailsById(5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(5L))
                .isInstanceOf(BookingAlreadyCancelledException.class);
    }

    private void givenMemberAndClass(GymClass gymClass) {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(gymClassRepository.findByIdForUpdate(gymClass.getId())).thenReturn(Optional.of(gymClass));
    }
}
