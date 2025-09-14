// src/main/java/atlan/evently/atlan/waitlist/service/WaitlistService.java
package atlan.evently.atlan.waitlist.service;

import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.booking.repo.BookingRepository;
import atlan.evently.atlan.event.model.Event;
import atlan.evently.atlan.event.repo.EventRepository;
import atlan.evently.atlan.notification.EmailNotificationService;
import atlan.evently.atlan.user.model.User;
import atlan.evently.atlan.user.service.UserService;
import atlan.evently.atlan.waitlist.model.WaitlistEntry;
import atlan.evently.atlan.waitlist.repo.WaitlistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class WaitlistService {

    private final WaitlistRepository waitlistRepo;
    private final EventRepository eventRepo;
    private final BookingRepository bookingRepo;
    private final UserService users;
    private final EmailNotificationService email; // NEW

    public WaitlistService(WaitlistRepository waitlistRepo,
                           EventRepository eventRepo,
                           BookingRepository bookingRepo,
                           UserService users,
                           EmailNotificationService email) { // NEW
        this.waitlistRepo = waitlistRepo;
        this.eventRepo = eventRepo;
        this.bookingRepo = bookingRepo;
        this.users = users;
        this.email = email; // NEW
    }

    // Add user to waitlist if event is at capacity; idempotent per (event,user)
    @Transactional
    public WaitlistEntry enqueue(Long eventId, Long userId) {
        Event e = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        User u = users.getById(userId);

        if (waitlistRepo.existsByEvent_IdAndUser_Id(eventId, userId)) {
            // Already enqueued; return existing (by querying earliest for this user+event)
            return waitlistRepo.findByUser_IdOrderByEnqueuedAtAsc(userId).stream()
                    .filter(w -> w.getEvent().getId().equals(eventId))
                    .findFirst()
                    .orElseGet(() -> {
                        WaitlistEntry w = new WaitlistEntry();
                        w.setEvent(e); w.setUser(u);
                        return waitlistRepo.save(w);
                    });
        }
        WaitlistEntry w = new WaitlistEntry();
        w.setEvent(e);
        w.setUser(u);
        return waitlistRepo.save(w);
    }

    // Attempt to promote the next user when a seat is available for this event.
    // Must be called inside the same transaction that freed a seat (e.g., cancel flow).
    @Transactional
    public Optional<Booking> promoteNextIfAvailable(Long eventId) {
        // Lock the event row to serialize capacity-critical operations
        Event e = eventRepo.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        // Compute availability from capacity - confirmed bookings
        long confirmedCount = bookingRepo.countByEvent_IdAndStatus(e.getId(), Booking.Status.CONFIRMED);
        boolean hasSeat = confirmedCount < e.getCapacity();
        if (!hasSeat) return Optional.empty();

        // Lock earliest waitlist row for this event
        Optional<WaitlistEntry> nextOpt = waitlistRepo.findTopByEvent_IdOrderByEnqueuedAtAscIdAsc(eventId);
        if (nextOpt.isEmpty()) return Optional.empty();

        WaitlistEntry next = nextOpt.get();
        User u = next.getUser();

        // Double-check the user doesn't already have an active booking
        boolean hasActive = bookingRepo.findByUser_IdAndEvent_IdAndStatus(u.getId(), e.getId(), Booking.Status.CONFIRMED).isPresent();
        if (hasActive) {
            waitlistRepo.delete(next);
            return promoteNextIfAvailable(eventId);
        }

        // Create booking and remove waitlist entry atomically
        Booking b = new Booking();
        b.setUser(u);
        b.setEvent(e);
        b.setStatus(Booking.Status.CONFIRMED);
        b.setBookedAt(OffsetDateTime.now());
        Booking saved = bookingRepo.save(b);

        waitlistRepo.delete(next);

        // Fire-and-forget email (async) so it doesn't block DB transaction
        email.sendWaitlistPromotion(u, e, saved);

        return Optional.of(saved);
    }
}
