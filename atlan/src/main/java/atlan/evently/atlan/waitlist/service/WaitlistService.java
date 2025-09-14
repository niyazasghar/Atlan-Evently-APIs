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
import atlan.evently.atlan.waitlist.web.dto.WaitlistItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class WaitlistService {

    private final WaitlistRepository waitlistRepo;
    private final EventRepository eventRepo;
    private final BookingRepository bookingRepo;
    private final UserService users;
    private final EmailNotificationService email;

    public WaitlistService(WaitlistRepository waitlistRepo,
                           EventRepository eventRepo,
                           BookingRepository bookingRepo,
                           UserService users,
                           EmailNotificationService email) {
        this.waitlistRepo = waitlistRepo;
        this.eventRepo = eventRepo;
        this.bookingRepo = bookingRepo;
        this.users = users;
        this.email = email;
    }

    private User currentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for principal"));
    }

    @Transactional
    public WaitlistEntry enqueueForCurrentUser(Long eventId) {
        Event e = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        User u = currentUserOrThrow();

        if (waitlistRepo.existsByEvent_IdAndUser_Id(eventId, u.getId())) {
            return waitlistRepo.findByUser_IdOrderByEnqueuedAtAsc(u.getId()).stream()
                    .filter(w -> w.getEvent().getId().equals(eventId))
                    .findFirst()
                    .orElseGet(() -> {
                        WaitlistEntry w = new WaitlistEntry();
                        w.setEvent(e);
                        w.setUser(u);
                        return waitlistRepo.save(w);
                    });
        }
        WaitlistEntry w = new WaitlistEntry();
        w.setEvent(e);
        w.setUser(u);
        return waitlistRepo.save(w);
    }

    // NEW: Return DTOs to avoid lazy serialization issues
    @Transactional(readOnly = true)
    public List<WaitlistItemResponse> myWaitlistForCurrentUserView() {
        User u = currentUserOrThrow();
        List<WaitlistEntry> entries = waitlistRepo.findByUserIdFetchEventOrderByEnqueued(u.getId());
        // 1-based position in this user's list
        return IntStream.range(0, entries.size())
                .mapToObj(i -> {
                    WaitlistEntry w = entries.get(i);
                    return new WaitlistItemResponse(
                            w.getId(),
                            w.getEvent().getId(),
                            w.getEvent().getName(),
                            w.getEnqueuedAt(),
                            i + 1
                    );
                })
                .toList();
    }

    @Transactional
    public Optional<Booking> promoteNextIfAvailable(Long eventId) {
        Event e = eventRepo.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        long confirmedCount = bookingRepo.countByEvent_IdAndStatus(e.getId(), Booking.Status.CONFIRMED);
        if (confirmedCount >= e.getCapacity()) return Optional.empty();

        Optional<WaitlistEntry> nextOpt = waitlistRepo.findTopByEvent_IdOrderByEnqueuedAtAscIdAsc(eventId);
        if (nextOpt.isEmpty()) return Optional.empty();

        WaitlistEntry next = nextOpt.get();
        User u = next.getUser();

        boolean hasActive = bookingRepo.findByUser_IdAndEvent_IdAndStatus(u.getId(), e.getId(), Booking.Status.CONFIRMED).isPresent();
        if (hasActive) {
            waitlistRepo.delete(next);
            return promoteNextIfAvailable(eventId);
        }

        Booking b = new Booking();
        b.setUser(u);
        b.setEvent(e);
        b.setStatus(Booking.Status.CONFIRMED);
        b.setBookedAt(OffsetDateTime.now());
        Booking saved = bookingRepo.save(b);

        waitlistRepo.delete(next);
        email.sendWaitlistPromotion(u, e, saved);
        return Optional.of(saved);
    }
}
