// src/main/java/atlan/evently/atlan/waitlist/service/WaitlistQueryService.java
package atlan.evently.atlan.waitlist.service;

import atlan.evently.atlan.user.model.User;
import atlan.evently.atlan.user.service.UserService;
import atlan.evently.atlan.waitlist.model.WaitlistEntry;
import atlan.evently.atlan.waitlist.repo.WaitlistRepository;
import atlan.evently.atlan.waitlist.web.dto.QueueStatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class WaitlistQueryService {
    private final WaitlistRepository waitlistRepo;
    private final UserService users;

    public WaitlistQueryService(WaitlistRepository waitlistRepo, UserService users) {
        this.waitlistRepo = waitlistRepo;
        this.users = users;
    }

    private User currentUserOrThrow() {
        var auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated"));
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for principal"));
    }

    public QueueStatusResponse myStatusForEvent(Long eventId) {
        var u = currentUserOrThrow();
        // find user’s entry for this event, earliest first
        var entries = waitlistRepo.findByUserIdFetchEventOrderByEnqueued(u.getId());
        var opt = entries.stream().filter(w -> w.getEvent().getId().equals(eventId)).findFirst();
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not on waitlist for this event");
        }
        WaitlistEntry w = opt.get();
        long ahead = waitlistRepo.countAhead(eventId, w.getEnqueuedAt(), w.getId());
        long total = waitlistRepo.countByEvent_Id(eventId);

        var dto = new QueueStatusResponse();
        dto.setEventId(eventId);
        dto.setEventName(w.getEvent().getName());
        dto.setPosition((int) (ahead + 1));
        dto.setTotal((int) total);
        dto.setEnqueuedAt(w.getEnqueuedAt());
        // naive estimate: 2 minutes per promotion; tune later
        dto.setEstimatedMinutes((int) Math.max(0, (total - (ahead + 1)) * 2));
        return dto;
    }
}
