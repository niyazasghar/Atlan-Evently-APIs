// src/main/java/atlan/evently/atlan/waitlist/controller/QrWaitlistController.java
package atlan.evently.atlan.waitlist.controller;

import atlan.evently.atlan.waitlist.qr.QrCodeService;
import atlan.evently.atlan.waitlist.service.WaitlistService;
import atlan.evently.atlan.waitlist.service.WaitlistQueryService;
import atlan.evently.atlan.waitlist.web.dto.QueueStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/waitlist/qr")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "QR Waitlist",
        description = "Generate event-specific QR codes and perform quick waitlist actions (join/status) for the authenticated user"
)
@Validated
public class QrWaitlistController {

    private final QrCodeService qr;
    private final WaitlistService waitlistService;
    private final WaitlistQueryService waitlistQuery;

    public QrWaitlistController(QrCodeService qr,
                                WaitlistService waitlistService,
                                WaitlistQueryService waitlistQuery) {
        this.qr = qr;
        this.waitlistService = waitlistService;
        this.waitlistQuery = waitlistQuery;
    }

    // Build absolute base URL from request headers (supports proxies)
    private String baseUrl(HttpServletRequest req) {
        String proto = req.getHeader("X-Forwarded-Proto");
        String host = req.getHeader("X-Forwarded-Host");
        if (proto != null && host != null) return proto + "://" + host;
        return req.getScheme() + "://" + req.getServerName() +
                ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort());
    }

    @Operation(
            summary = "QR image to join event waitlist",
            description = "Returns a PNG QR code that encodes a direct link to join the event's waitlist for quick self check-in.",
            parameters = {
                    @Parameter(
                            name = "eventId",
                            description = "ID of the event for which to generate the QR",
                            required = true,
                            in = ParameterIn.QUERY,
                            example = "77"
                    ),
                    @Parameter(
                            name = "size",
                            description = "PNG size in pixels (min 160, max 1024). Defaults to 320.",
                            required = false,
                            in = ParameterIn.QUERY,
                            example = "320"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "PNG image returned",
                            content = @Content(
                                    mediaType = "image/png",
                                    schema = @Schema(type = "string", format = "binary")
                            ),
                            headers = {
                                    @Header(
                                            name = "Cache-Control",
                                            description = "Cache directive (set to no-cache,no-store,must-revalidate for freshness)",
                                            schema = @Schema(type = "string"),
                                            required = false
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid token)"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            }
    )
    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrImage(
            @RequestParam Long eventId,
            @RequestParam(defaultValue = "320") int size,
            HttpServletRequest request
    ) {
        String joinUrl = baseUrl(request) + "/api/v1/waitlist/join?eventId=" + eventId;
        byte[] png = qr.pngForUrl(joinUrl, Math.max(160, Math.min(size, 1024)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    @Operation(
            summary = "Join event waitlist (current user)",
            description = "Adds the authenticated user to the waitlist for the provided event; idempotent per (event,user).",
            parameters = {
                    @Parameter(
                            name = "eventId",
                            description = "ID of the event to join",
                            required = true,
                            in = ParameterIn.QUERY,
                            example = "77"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Joined or already present on waitlist"),
                    @ApiResponse(responseCode = "400", description = "Invalid eventId"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid token)"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Event not found"),
                    @ApiResponse(responseCode = "409", description = "Constraint violation"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            }
    )
    @PostMapping("/join")
    public ResponseEntity<Void> join(@RequestParam Long eventId) {
        waitlistService.enqueueForCurrentUser(eventId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "My waitlist status (current user)",
            description = "Returns the authenticated user's position in the event waitlist, total queue length, and basic ETA.",
            parameters = {
                    @Parameter(
                            name = "eventId",
                            description = "ID of the event to query",
                            required = true,
                            in = ParameterIn.QUERY,
                            example = "77"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Queue status returned",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = QueueStatusResponse.class),
                                    examples = @ExampleObject(value = """
                    {
                      "eventId": 77,
                      "eventName": "AI Summit",
                      "position": 3,
                      "total": 18,
                      "enqueuedAt": "2025-09-14T16:40:00Z",
                      "estimatedMinutes": 30
                    }
                    """)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid token)"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not on waitlist for this event"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            }
    )
    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> myStatus(@RequestParam Long eventId) {
        return ResponseEntity.ok(waitlistQuery.myStatusForEvent(eventId));
    }
}
