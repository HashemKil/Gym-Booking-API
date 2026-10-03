package com.gymbooking.service;

import com.gymbooking.dto.BookingResponse;
import com.gymbooking.dto.MemberRequest;
import com.gymbooking.dto.MemberResponse;
import com.gymbooking.entity.Member;
import com.gymbooking.exception.EmailAlreadyUsedException;
import com.gymbooking.exception.ResourceNotFoundException;
import com.gymbooking.repository.BookingRepository;
import com.gymbooking.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BookingRepository bookingRepository;
    private final Clock clock;

    public MemberService(MemberRepository memberRepository, BookingRepository bookingRepository, Clock clock) {
        this.memberRepository = memberRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    @Transactional
    public MemberResponse register(MemberRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (memberRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        Member member = new Member(request.fullName().trim(), email, LocalDateTime.now(clock));
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return memberRepository.findAllByOrderByFullNameAsc().stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        return MemberResponse.from(getMemberOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findBookings(Long id) {
        getMemberOrThrow(id);
        return bookingRepository.findByMemberIdOrderByBookedAtDesc(id).stream()
                .map(BookingResponse::from)
                .toList();
    }

    private Member getMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }
}
