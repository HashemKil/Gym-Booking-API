package com.gymbooking.service;

import com.gymbooking.dto.BookingResponse;
import com.gymbooking.dto.GymClassRequest;
import com.gymbooking.dto.GymClassResponse;
import com.gymbooking.entity.BookingStatus;
import com.gymbooking.entity.GymClass;
import com.gymbooking.exception.ClassHasBookingsException;
import com.gymbooking.exception.ResourceNotFoundException;
import com.gymbooking.repository.BookingRepository;
import com.gymbooking.repository.ClassBookingCount;
import com.gymbooking.repository.GymClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GymClassService {

    private final GymClassRepository gymClassRepository;
    private final BookingRepository bookingRepository;

    public GymClassService(GymClassRepository gymClassRepository, BookingRepository bookingRepository) {
        this.gymClassRepository = gymClassRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public GymClassResponse create(GymClassRequest request) {
        GymClass gymClass = new GymClass(
                request.name().trim(),
                request.instructor().trim(),
                request.startTime(),
                request.durationMinutes(),
                request.capacity()
        );
        return GymClassResponse.from(gymClassRepository.save(gymClass), 0);
    }

    @Transactional(readOnly = true)
    public List<GymClassResponse> findAll() {
        Map<Long, Long> activeCounts = bookingRepository.countPerClassByStatus(BookingStatus.ACTIVE).stream()
                .collect(Collectors.toMap(ClassBookingCount::getClassId, ClassBookingCount::getTotal));

        return gymClassRepository.findAllByOrderByStartTimeAsc().stream()
                .map(gymClass -> GymClassResponse.from(gymClass, activeCounts.getOrDefault(gymClass.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public GymClassResponse findById(Long id) {
        GymClass gymClass = getClassOrThrow(id);
        return GymClassResponse.from(gymClass, activeBookings(id));
    }

    @Transactional
    public GymClassResponse update(Long id, GymClassRequest request) {
        GymClass gymClass = gymClassRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", id));

        long booked = activeBookings(id);
        if (request.capacity() < booked) {
            throw new ClassHasBookingsException(
                    "Capacity cannot be lower than the " + booked + " active bookings for this class");
        }

        gymClass.update(
                request.name().trim(),
                request.instructor().trim(),
                request.startTime(),
                request.durationMinutes(),
                request.capacity()
        );
        return GymClassResponse.from(gymClass, booked);
    }

    @Transactional
    public void delete(Long id) {
        GymClass gymClass = gymClassRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", id));

        if (activeBookings(id) > 0) {
            throw new ClassHasBookingsException("Cannot delete a class that still has active bookings");
        }

        bookingRepository.deleteByGymClassId(id);
        gymClassRepository.delete(gymClass);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findBookings(Long id) {
        getClassOrThrow(id);
        return bookingRepository.findByGymClassIdOrderByBookedAtAsc(id).stream()
                .map(BookingResponse::from)
                .toList();
    }

    private GymClass getClassOrThrow(Long id) {
        return gymClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", id));
    }

    private long activeBookings(Long classId) {
        return bookingRepository.countByGymClassIdAndStatus(classId, BookingStatus.ACTIVE);
    }
}
