// src/main/java/atlan/evently/atlan/waitlist/controller/WaitlistController.java
package atlan.evently.atlan.waitlist.controller;

import atlan.evently.atlan.waitlist.model.WaitlistEntry;
import atlan.evently.atlan.waitlist.repo.WaitlistRepository;
import atlan.evently.atlan.waitlist.service.WaitlistService;
import atlan.evently.atlan.waitlist.web.dto.WaitlistItemResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/waitlist")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Waitlist", description = "Join and view the waitlist for events; promotion occurs when seats free up")
@Validated
public class WaitlistController {

    private final WaitlistService waitlist;
    private final WaitlistRepository waitlistRepo;

    public WaitlistController(WaitlistService waitlist, WaitlistRepository waitlistRepo) {
        this.waitlist = waitlist;
        this.waitlistRepo = waitlistRepo;
    }

    @Operation(summary = "Join event waitlist (current user)")
    @PostMapping
    public ResponseEntity<WaitlistEntry> enqueue(@RequestParam Long eventId) {
        return ResponseEntity.ok(waitlist.enqueueForCurrentUser(eventId));
    }

    @Operation(summary = "My waitlist (current user)")
    @GetMapping("/me")
    public ResponseEntity<List<WaitlistItemResponse>> myWaitlist() {
        return ResponseEntity.ok(waitlist.myWaitlistForCurrentUserView());
    }
}
