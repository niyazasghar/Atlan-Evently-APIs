package atlan.evently.atlan.booking.web.controller;

import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.booking.service.BookingService;
import atlan.evently.atlan.booking.web.BookingMapper;
import atlan.evently.atlan.booking.web.dto.BookingResponse;
import atlan.evently.atlan.booking.web.dto.BookingCreateRequest;
import atlan.evently.atlan.idempotency.IdempotencyUtil;
import atlan.evently.atlan.idempotency.service.IdempotencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

 // This shows lock & requires auth
import java.util.List;

/**
 * REST Controller for managing event bookings.
 * <p>
 * This controller provides public endpoints for creating, canceling, and retrieving bookings.
 * Operations support idempotent creation of bookings to avoid duplication.
 * All endpoints operate under the base path "/api/v1/bookings".
 */
@RestController
@RequestMapping("/api/v1/bookings")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Booking Management", description = "APIs for creating, cancelling, and retrieving bookings")
public class BookingController {

    private final BookingService bookings;
    private final IdempotencyService idempotencyService;

    /**
     * Constructs a new BookingController with required dependencies.
     *
     * @param bookings           The BookingService handling business logic for bookings.
     * @param idempotencyService The service managing idempotency behavior and state.
     */
    public BookingController(BookingService bookings, IdempotencyService idempotencyService) {
        this.bookings = bookings;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Creates a new booking for a user at a specific event.
     * <p>
     * Requires a unique {@code Idempotency-Key} header to support safe retries.
     *
     * @param idemKey The idempotency key provided by the client to prevent duplicate processing.
     * @param req     The booking request containing user ID and event ID.
     * @return A {@link ResponseEntity} containing the booking details and HTTP status.
     */
    @Operation(
            summary = "Create a new booking",
            description = "Creates a booking linking a user to an event. Supports idempotent retries via Idempotency-Key header."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking created successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (missing or malformed user/event ID)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User or Event not found",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate request or request in progress",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @Parameter(description = "Unique idempotency key to ensure safe retries", required = true)
            @RequestHeader("Idempotency-Key") String idemKey,
            @Valid @RequestBody BookingCreateRequest req) {

        String endpoint = "POST:/api/v1/bookings";
        String requestHash = IdempotencyUtil.sha256(req.getUserId() + ":" + req.getEventId());

        return idempotencyService.executeCreateBooking(
                idemKey, endpoint, req.getUserId(), requestHash,
                () -> bookings.createBooking(req.getUserId(), req.getEventId())
        );
    }

    /**
     * Cancels a booking by its unique ID.
     *
     * @param id The booking ID.
     * @return A {@link ResponseEntity} with updated booking details.
     */
    @Operation(
            summary = "Cancel an existing booking",
            description = "Marks the specified booking as cancelled."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponse> cancel(
            @Parameter(description = "Booking ID to cancel", required = true)
            @PathVariable Long id) {
        Booking booking = bookings.cancelBooking(id);
        return ResponseEntity.ok(BookingMapper.toResponse(booking));
    }

    /**
     * Lists all bookings made by a specific user.
     *
     * @param userId The user ID.
     * @return A list of bookings for the user.
     */
    @Operation(
            summary = "List bookings by user",
            description = "Retrieves a list of all bookings (past and present) made by the specified user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> listByUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        List<Booking> bookingsList = bookings.listUserBookings(userId);
        return ResponseEntity.ok(BookingMapper.toResponseList(bookingsList));
    }
}
