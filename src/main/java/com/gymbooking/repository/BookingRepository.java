package com.gymbooking.repository;

import com.gymbooking.entity.Booking;
import com.gymbooking.entity.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByGymClassIdAndStatus(Long gymClassId, BookingStatus status);

    boolean existsByMemberIdAndGymClassIdAndStatus(Long memberId, Long gymClassId, BookingStatus status);

    @EntityGraph(attributePaths = {"member", "gymClass"})
    Optional<Booking> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"member", "gymClass"})
    List<Booking> findByGymClassIdOrderByBookedAtAsc(Long gymClassId);

    @EntityGraph(attributePaths = {"member", "gymClass"})
    List<Booking> findByMemberIdOrderByBookedAtDesc(Long memberId);

    @Query("""
            select b.gymClass.id as classId, count(b) as total
            from Booking b
            where b.status = :status
            group by b.gymClass.id
            """)
    List<ClassBookingCount> countPerClassByStatus(@Param("status") BookingStatus status);

    void deleteByGymClassId(Long gymClassId);
}
