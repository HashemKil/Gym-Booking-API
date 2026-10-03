package com.gymbooking.repository;

import com.gymbooking.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmailIgnoreCase(String email);

    List<Member> findAllByOrderByFullNameAsc();
}
