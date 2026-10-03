package com.gymbooking.dto;

import com.gymbooking.entity.Member;

import java.time.LocalDateTime;

public record MemberResponse(Long id, String fullName, String email, LocalDateTime joinedAt) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getFullName(), member.getEmail(), member.getJoinedAt());
    }
}
