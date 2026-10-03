package com.gymbooking.config;

import com.gymbooking.entity.Booking;
import com.gymbooking.entity.GymClass;
import com.gymbooking.entity.Member;
import com.gymbooking.repository.BookingRepository;
import com.gymbooking.repository.GymClassRepository;
import com.gymbooking.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final GymClassRepository gymClassRepository;
    private final MemberRepository memberRepository;
    private final BookingRepository bookingRepository;
    private final Clock clock;

    public DataSeeder(GymClassRepository gymClassRepository,
                      MemberRepository memberRepository,
                      BookingRepository bookingRepository,
                      Clock clock) {
        this.gymClassRepository = gymClassRepository;
        this.memberRepository = memberRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (gymClassRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now(clock);
        LocalDateTime now = LocalDateTime.now(clock);

        GymClass yoga = gymClassRepository.save(new GymClass(
                "Morning Yoga", "Lina Haddad", today.plusDays(1).atTime(LocalTime.of(7, 0)), 60, 15));
        GymClass hiit = gymClassRepository.save(new GymClass(
                "HIIT Blast", "Omar Saleh", today.plusDays(1).atTime(LocalTime.of(18, 0)), 45, 3));
        gymClassRepository.save(new GymClass(
                "Spin Class", "Rami Khoury", today.plusDays(2).atTime(LocalTime.of(19, 0)), 50, 20));
        gymClassRepository.save(new GymClass(
                "Boxing Fundamentals", "Sara Nasser", today.plusDays(3).atTime(LocalTime.of(17, 30)), 60, 12));
        gymClassRepository.save(new GymClass(
                "Pilates Core", "Dana Aziz", today.minusDays(1).atTime(LocalTime.of(8, 0)), 50, 10));

        Member ahmad = memberRepository.save(new Member("Ahmad Ali", "ahmad.ali@example.com", now));
        Member layla = memberRepository.save(new Member("Layla Mansour", "layla.mansour@example.com", now));
        memberRepository.save(new Member("Yousef Karim", "yousef.karim@example.com", now));
        memberRepository.save(new Member("Noor Hamdan", "noor.hamdan@example.com", now));

        bookingRepository.save(new Booking(ahmad, hiit, now));
        bookingRepository.save(new Booking(layla, hiit, now));
        bookingRepository.save(new Booking(ahmad, yoga, now));

        log.info("Loaded sample data: 5 classes, 4 members, 3 bookings");
    }
}
