package com.gymbooking.repository;

import com.gymbooking.entity.GymClass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    List<GymClass> findAllByOrderByStartTimeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GymClass g where g.id = :id")
    Optional<GymClass> findByIdForUpdate(@Param("id") Long id);
}
