// src/main/java/atlan/evently/atlan/event/web/controller/EventController.java
package atlan.evently.atlan.event.web.controller;

import atlan.evently.atlan.event.model.Event;
import atlan.evently.atlan.event.service.EventService;
import atlan.evently.atlan.event.web.dto.EventCreateRequest;
import atlan.evently.atlan.event.web.dto.EventResponse;
import atlan.evently.atlan.event.web.dto.EventUpdateRequest;
import atlan.evently.atlan.event.web.EventMapper;
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

import java.util.List;

/**
 * REST Controller for managing events.
 * <p>
 * Provides public endpoints for browsing events and administrative endpoints for
 * creating and updating them. All endpoints operate under the base path "/api/v1/events".
 */
@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event Management", description = "APIs for creating, updating, and browsing events")
public class EventController {

    private final EventService events;

    /**
     * Constructs an EventController with the necessary EventService dependency.
     *
     * @param events The service layer responsible for event business logic.
     */
    public EventController(EventService events) {
        this.events = events;
    }

    /**
     * Retrieves a paginated list of upcoming events.
     * This endpoint is publicly accessible.
     *
     * @param page The page number to retrieve, starting from 0.
     * @param size The number of events per page.
     * @return A ResponseEntity containing a list of event details. The list can be empty.
     */
    @Operation(
            summary = "List upcoming events",
            description = "Provides a paginated list of all upcoming events, available to all users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of events", content = { @Content(schema = @Schema(implementation = EventResponse.class), mediaType = "application/json") })
    })
    @GetMapping
    public ResponseEntity<List<EventResponse>> list(
            @Parameter(description = "The page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The number of events per page") @RequestParam(defaultValue = "20") int size) {
        List<Event> upcoming = events.listUpcoming(page, size);
        return ResponseEntity.ok(EventMapper.toResponseList(upcoming));
    }

    /**
     * Creates a new event.
     * This is an administrative endpoint.
     *
     * @param req The request body containing the new event's details.
     * @return A ResponseEntity containing the created event's details and an HTTP 200 OK status.
     */
    @Operation(
            summary = "Create a new event (Admin)",
            description = "Creates a new event with the specified details. Requires administrative privileges."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event created successfully", content = { @Content(schema = @Schema(implementation = EventResponse.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid input, such as invalid dates or missing fields", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "403", description = "Forbidden, user does not have admin rights", content = { @Content(schema = @Schema()) })
    })
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventCreateRequest req) {
        Event e = events.create(req.getName(), req.getVenue(), req.getStartTime(), req.getEndTime(), req.getCapacity());
        return ResponseEntity.ok(EventMapper.toResponse(e));
    }

    /**
     * Updates an existing event's details.
     * This is an administrative endpoint.
     *
     * @param id  The unique identifier of the event to update.
     * @param req The request body containing the fields to update.
     * @return A ResponseEntity containing the updated event's details and an HTTP 200 OK status.
     */
    @Operation(
            summary = "Update an existing event (Admin)",
            description = "Updates the details of an existing event by its ID. Requires administrative privileges."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event updated successfully", content = { @Content(schema = @Schema(implementation = EventResponse.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid input for the fields to be updated", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "403", description = "Forbidden, user does not have admin rights", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", description = "Event not found with the given ID", content = { @Content(schema = @Schema()) })
    })
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(
            @Parameter(description = "Unique ID of the event to update") @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest req) {
        Event e = events.update(id, req.getName(), req.getVenue(), req.getStartTime(), req.getEndTime(), req.getCapacity());
        return ResponseEntity.ok(EventMapper.toResponse(e));
    }
}