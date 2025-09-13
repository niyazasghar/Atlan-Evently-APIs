package atlan.evently.atlan.booking.service;

// src/main/java/atlan/evently/atlan/booking/service/BookingService.java
import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.booking.repo.BookingRepository;
import atlan.evently.atlan.caching.policy.DoNotCache;
import atlan.evently.atlan.event.model.Event;
import atlan.evently.atlan.event.repo.EventRepository;
import atlan.evently.atlan.user.model.User;
import atlan.evently.atlan.user.service.UserService;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@DoNotCache
public class BookingService {
    private final BookingRepository bookings;
    private final EventRepository events;
    private final UserService users;

    public BookingService(BookingRepository bookings, EventRepository events, UserService users) {
        this.bookings = bookings;
        this.events = events;
        this.users = users;
    }
    @DoNotCache
    @Transactional
    public Booking createBooking(Long userId, Long eventId) {
        int attempts = 0;
        while (true) {
            try {
                return attemptCreate(userId, eventId);
            } catch (OptimisticLockException e) {
                if (++attempts >= 3) throw e;
            }
        }
    }

    private Booking attemptCreate(Long userId, Long eventId) {
        User u = users.getById(userId);
        Event e = events.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        bookings.findByUser_IdAndEvent_IdAndStatus(u.getId(), e.getId(), Booking.Status.CONFIRMED)
                .ifPresent(b -> { throw new IllegalStateException("Active booking already exists"); });

        long activeCount = bookings.countByEvent_IdAndStatus(e.getId(), Booking.Status.CONFIRMED);
        if (activeCount >= e.getCapacity()) {
            throw new IllegalStateException("Event at capacity");
        }

        Booking b = new Booking();
        b.setUser(u);
        b.setEvent(e);
        b.setStatus(Booking.Status.CONFIRMED);
        b.setBookedAt(OffsetDateTime.now());
        return bookings.save(b);
    }
    @DoNotCache
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking b = bookings.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (b.getStatus() == Booking.Status.CANCELED) {
            return b;
        }
        events.findByIdForUpdate(b.getEvent().getId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        b.setStatus(Booking.Status.CANCELED);
        b.setCanceledAt(OffsetDateTime.now());
        return b;
    }
    @DoNotCache
    public List<Booking> listUserBookings(Long userId) {
        return bookings.findByUser_IdOrderByBookedAtDesc(userId);
    }
    /**
     * Example of a live availability check — intentionally not cached.
     * Implementations should compute directly from DB (capacity - confirmed bookings).
     */
    @DoNotCache
    public boolean isAvailable(Long eventId) {
        Event e = events.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        long activeCount = bookings.countByEvent_IdAndStatus(e.getId(), Booking.Status.CONFIRMED);
        return activeCount < e.getCapacity();
    }
}
