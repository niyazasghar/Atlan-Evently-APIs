package atlan.evently.atlan.booking.web.controller;

import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.booking.service.BookingService;
import atlan.evently.atlan.booking.web.BookingMapper;
import atlan.evently.atlan.booking.web.dto.BookingCreateRequest;
import atlan.evently.atlan.booking.web.dto.BookingResponse;
import atlan.evently.atlan.idempotency.IdempotencyUtil;
import atlan.evently.atlan.idempotency.service.IdempotencyService;
import atlan.evently.atlan.user.model.User;
import atlan.evently.atlan.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing event bookings.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Booking Management", description = "APIs for creating, cancelling, and retrieving bookings")
public class BookingController {

    private final BookingService bookings;
    private final IdempotencyService idempotencyService;
    private final UserService users;

    public BookingController(BookingService bookings, IdempotencyService idempotencyService, UserService users) {
        this.bookings = bookings;
        this.idempotencyService = idempotencyService;
        this.users = users;
    }

    @Operation(summary = "Create a new booking",
            description = "Creates a booking linking a user to an event. Supports idempotent retries via Idempotency-Key header.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking created successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "404", description = "User or Event not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate request or request in progress", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @Parameter(description = "Unique idempotency key to ensure safe retries", required = true)
            @RequestHeader("Idempotency-Key") String idemKey,
            @Valid @RequestBody BookingCreateRequest req
    ) {
        String endpoint = "POST:/api/v1/bookings";
        String requestHash = IdempotencyUtil.sha256(req.getUserId() + ":" + req.getEventId());
        return idempotencyService.executeCreateBooking(
                idemKey, endpoint, req.getUserId(), requestHash,
                () -> bookings.createBooking(req.getUserId(), req.getEventId())
        );
    }

    @Operation(summary = "Cancel an existing booking (legacy)",
            description = "Marks the specified booking as cancelled and frees a seat; same semantics as /{id}/cancel.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Booking not cancellable", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponse> cancelLegacy(@PathVariable Long id, Authentication auth) {
        Long requesterUserId = resolveRequesterUserId(auth);
        boolean isAdmin = hasAdmin(auth);
        Booking booking = bookings.cancelBooking(id, requesterUserId, isAdmin);
        return ResponseEntity.ok(BookingMapper.toResponse(booking));
    }

    @Operation(summary = "Cancel booking (POST)",
            description = "Cancels the booking and frees a seat within one transaction; returns the updated booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "409", description = "Booking not cancellable", content = @Content)
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBookingPost(@PathVariable Long id, Authentication auth) {
        Long requesterUserId = resolveRequesterUserId(auth);
        boolean isAdmin = hasAdmin(auth);
        Booking booking = bookings.cancelBooking(id, requesterUserId, isAdmin);
        return ResponseEntity.ok(BookingMapper.toResponse(booking));
    }

    @Operation(summary = "Cancel booking (DELETE)",
            description = "Cancels the booking and frees a seat within one transaction; returns the updated booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "409", description = "Booking not cancellable", content = @Content)
    })
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBookingDelete(@PathVariable Long id, Authentication auth) {
        Long requesterUserId = resolveRequesterUserId(auth);
        boolean isAdmin = hasAdmin(auth);
        Booking booking = bookings.cancelBooking(id, requesterUserId, isAdmin);
        return ResponseEntity.ok(BookingMapper.toResponse(booking));
    }

    @Operation(summary = "List bookings by user",
            description = "Retrieves all bookings (past and present) made by the specified user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> listByUser(@PathVariable Long userId) {
        List<Booking> bookingsList = bookings.listUserBookings(userId);
        return ResponseEntity.ok(BookingMapper.toResponseList(bookingsList));
    }

    private Long resolveRequesterUserId(Authentication auth) {
        if (auth == null) return null;
        String username = auth.getName(); // subject set by JwtAuthFilter (typically email/username)
        return users.findByEmail(username).map(User::getId).orElse(null);
    }

    private boolean hasAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
