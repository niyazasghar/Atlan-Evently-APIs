package atlan.evently.atlan.booking.repo;

// src/main/java/com/evently/booking/repo/BookingRepository.java

import java.util.List;
import java.util.Optional;

import atlan.evently.atlan.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    long countByEvent_IdAndStatus(Long eventId, Booking.Status status);
    Optional<Booking> findByUser_IdAndEvent_IdAndStatus(Long userId, Long eventId, Booking.Status status);
    List<Booking> findByUser_IdOrderByBookedAtDesc(Long userId);
}
