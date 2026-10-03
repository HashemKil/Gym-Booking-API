package com.gymbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gym_class_id", nullable = false)
    private GymClass gymClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(nullable = false)
    private LocalDateTime bookedAt;

    private LocalDateTime cancelledAt;

    protected Booking() {
    }

    public Booking(Member member, GymClass gymClass, LocalDateTime bookedAt) {
        this.member = member;
        this.gymClass = gymClass;
        this.bookedAt = bookedAt;
        this.status = BookingStatus.ACTIVE;
    }

    public void cancel(LocalDateTime when) {
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = when;
    }

    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public GymClass getGymClass() {
        return gymClass;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
}
