// src/main/java/atlan/evently/atlan/waitlist/controller/WaitlistController.java
package atlan.evently.atlan.waitlist.controller;

import atlan.evently.atlan.waitlist.model.WaitlistEntry;
import atlan.evently.atlan.waitlist.repo.WaitlistRepository;
import atlan.evently.atlan.waitlist.service.WaitlistService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// OpenAPI / Swagger annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
// import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/waitlist")
// @SecurityRequirement(name = "bearerAuth") // uncomment if this API requires auth in your OpenAPI config
@Tag(
        name = "Waitlist",
        description = "Join and view the waitlist for events; promoting from waitlist is handled by the service when seats free up"
)
@Validated
public class WaitlistController {

    private final WaitlistService waitlist;
    private final WaitlistRepository waitlistRepo;

    public WaitlistController(WaitlistService waitlist, WaitlistRepository waitlistRepo) {
        this.waitlist = waitlist;
        this.waitlistRepo = waitlistRepo;
    }

    @Operation(
            summary = "Join event waitlist",
            description = "Adds the specified user to the waitlist for the given event if capacity is full; idempotent per (eventId, userId)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User enqueued (or already present) on the waitlist",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WaitlistEntry.class),
                            examples = @ExampleObject(value = """
                {
                  "id": 1234,
                  "event": {"id": 77},
                  "user": {"id": 42},
                  "enqueuedAt": "2025-09-14T12:34:56Z"
                }
                """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid eventId or userId"),
            @ApiResponse(responseCode = "404", description = "Event or user not found"),
            @ApiResponse(responseCode = "409", description = "Constraint violation (duplicate waitlist entry)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping
    public ResponseEntity<WaitlistEntry> enqueue(
            @Parameter(name = "eventId", description = "ID of the event", in = ParameterIn.QUERY, example = "77")
            @RequestParam @Min(1) Long eventId,
            @Parameter(name = "userId", description = "ID of the user", in = ParameterIn.QUERY, example = "42")
            @RequestParam @Min(1) Long userId
    ) {
        return ResponseEntity.ok(waitlist.enqueue(eventId, userId));
    }

    @Operation(
            summary = "List my waitlist",
            description = "Returns the user’s waitlist entries ordered by enqueue time ascending."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Waitlist entries fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = WaitlistEntry.class)),
                            examples = @ExampleObject(value = """
                [
                  {
                    "id": 1234,
                    "event": {"id": 77},
                    "user": {"id": 42},
                    "enqueuedAt": "2025-09-14T12:34:56Z"
                  },
                  {
                    "id": 1235,
                    "event": {"id": 80},
                    "user": {"id": 42},
                    "enqueuedAt": "2025-09-15T08:10:00Z"
                  }
                ]
                """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid userId"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WaitlistEntry>> myWaitlist(
            @Parameter(name = "userId", description = "ID of the user", in = ParameterIn.PATH, example = "42")
            @PathVariable @Min(1) Long userId
    ) {
        return ResponseEntity.ok(waitlistRepo.findByUser_IdOrderByEnqueuedAtAsc(userId));
    }
}
