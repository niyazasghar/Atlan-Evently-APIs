package atlan.evently.atlan.booking.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    /**
     * Create a booking atomically with concurrency control:
     * - Locks the Event row (via repository method) so concurrent writers collide predictably.
     * - Prevents duplicate active booking for the same user and event.
     * - Enforces capacity using the current confirmed count vs. event capacity.
     * Retries on OptimisticLockException a few times to tolerate races.
     */
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

        // Lock event row (repository uses OPTIMISTIC_FORCE_INCREMENT/PESSIMISTIC lock under the hood)
        Event e = events.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Guard: no duplicate active booking per user+event
        bookings.findByUser_IdAndEvent_IdAndStatus(u.getId(), e.getId(), Booking.Status.CONFIRMED)
                .ifPresent(b -> { throw new IllegalStateException("Active booking already exists"); });

        // Capacity guard (confirmed count < capacity)
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

    /**
     * Cancel a booking with ownership/admin enforcement:
     * - Admin path locks booking by id; owner path locks booking by (id,userId).
     * - If already cancelled, returns as-is (idempotent).
     * - Locks the Event row to serialize capacity-affecting operations.
     * Capacity is derived from confirmed count vs. capacity, so no explicit counter increment is needed.
     */
    @DoNotCache
    @Transactional
    public Booking cancelBooking(Long bookingId, Long requesterUserId, boolean isAdmin) {
        Booking b;
        if (isAdmin) {
            b = bookings.findByIdForUpdate(bookingId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        } else {
            if (requesterUserId == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthenticated");
            }
            b = bookings.findByIdAndUserIdForUpdate(bookingId, requesterUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to cancel this booking"));
        }

        if (b.getStatus() == Booking.Status.CANCELED) {
            return b;
        }

        // Lock event row to keep capacity-affecting operations serialized
        events.findByIdForUpdate(b.getEvent().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        // Flip status
        b.setStatus(Booking.Status.CANCELED);
        b.setCanceledAt(OffsetDateTime.now());
        return b;
    }

    @DoNotCache
    public List<Booking> findAll() {
        return bookings.findAll();
    }

    @DoNotCache
    public List<Booking> listUserBookings(Long userId) {
        return bookings.findByUser_IdOrderByBookedAtDesc(userId);
    }

    /**
     * Live availability computed from capacity - confirmed; avoids stale cache.
     */
    @DoNotCache
    public boolean isAvailable(Long eventId) {
        Event e = events.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        long activeCount = bookings.countByEvent_IdAndStatus(e.getId(), Booking.Status.CONFIRMED);
        return activeCount < e.getCapacity();
    }
}
