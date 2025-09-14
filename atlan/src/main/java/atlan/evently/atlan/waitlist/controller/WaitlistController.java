// src/main/java/atlan/evently/atlan/waitlist/controller/WaitlistController.java
package atlan.evently.atlan.waitlist.controller;

import atlan.evently.atlan.waitlist.model.WaitlistEntry;
import atlan.evently.atlan.waitlist.repo.WaitlistRepository;
import atlan.evently.atlan.waitlist.service.WaitlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/waitlist")
public class WaitlistController {

    private final WaitlistService waitlist;
    private final WaitlistRepository waitlistRepo;

    public WaitlistController(WaitlistService waitlist, WaitlistRepository waitlistRepo) {
        this.waitlist = waitlist;
        this.waitlistRepo = waitlistRepo;
    }

    @PostMapping
    public ResponseEntity<WaitlistEntry> enqueue(@RequestParam Long eventId, @RequestParam Long userId) {
        return ResponseEntity.ok(waitlist.enqueue(eventId, userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WaitlistEntry>> myWaitlist(@PathVariable Long userId) {
        return ResponseEntity.ok(waitlistRepo.findByUser_IdOrderByEnqueuedAtAsc(userId));
    }
}
