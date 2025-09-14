// src/main/java/atlan/evently/atlan/analytics/controller/AdvancedAnalyticsController.java
package atlan.evently.atlan.analytics.controller;

import atlan.evently.atlan.analytics.dto.*;
import atlan.evently.atlan.analytics.service.AdvancedAnalyticsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@SecurityRequirement(name = "bearerAuth") // Requires a valid JWT bearer token
@Tag(
        name = "Admin Analytics",
        description = "Admin-only analytics APIs for totals, most-booked events, cancellation rates, and daily booking statistics"
)
@Validated
public class AdvancedAnalyticsController {

    private final AdvancedAnalyticsService svc;

    public AdvancedAnalyticsController(AdvancedAnalyticsService svc) {
        this.svc = svc;
    }

    @Operation(
            summary = "Get aggregate totals",
            description = "Returns total events, total bookings, confirmed bookings, and cancelled bookings. Admin-only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Aggregates fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnalyticsTotals.class),
                            examples = @ExampleObject(value = """
                {
                  "totalEvents": 42,
                  "totalBookings": 1280,
                  "confirmedBookings": 1100,
                  "cancelledBookings": 180
                }
                """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid token)"),
            @ApiResponse(responseCode = "403", description = "Forbidden (requires ROLE_ADMIN)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/totals")
    public ResponseEntity<AnalyticsTotals> totals() {
        return ResponseEntity.ok(svc.totals());
    }

    @Operation(
            summary = "Top N most-booked events",
            description = "Returns the top events ranked by confirmed booking count. Admin-only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Top events fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MostBookedEventView.class)),
                            examples = @ExampleObject(value = """
                [
                  {"eventId": 7, "eventName": "Tech Talk", "confirmedCount": 220},
                  {"eventId": 3, "eventName": "AI Summit", "confirmedCount": 180}
                ]
                """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid limit value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid token)"),
            @ApiResponse(responseCode = "403", description = "Forbidden (requires ROLE_ADMIN)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/most-booked")
    public ResponseEntity<List<MostBookedEventView>> mostBooked(
            @Parameter(
                    name = "limit",
                    description = "Maximum number of rows to return (1–100). Defaults to 10.",
                    in = ParameterIn.QUERY,
                    example = "10"
            )
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        return ResponseEntity.ok(svc.mostBooked(limit));
    }

    @Operation(
            summary = "Cancellation rates by event",
            description = "Returns per-event confirmed and cancelled counts and the cancellation rate percentage. Admin-only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cancellation rates fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CancellationRateView.class)),
                            examples = @ExampleObject(value = """
                [
                  {"eventId": 7, "eventName": "Tech Talk", "confirmedCount": 220, "cancelledCount": 25, "cancellationRatePercent": 10.2},
                  {"eventId": 3, "eventName": "AI Summit", "confirmedCount": 180, "cancelledCount": 18, "cancellationRatePercent": 9.1}
                ]
                """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid token)"),
            @ApiResponse(responseCode = "403", description = "Forbidden (requires ROLE_ADMIN)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/cancellation-rates")
    public ResponseEntity<List<CancellationRateView>> cancellationRates() {
        return ResponseEntity.ok(svc.cancellationRates());
    }

//    @Operation(
//            summary = "Daily booking statistics",
//            description = "Returns day-by-day counts of bookings and cancellations for the given date range [from, to]. Admin-only."
//    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Daily stats fetched successfully",
//                    content = @Content(
//                            mediaType = "application/json",
//                            array = @ArraySchema(schema = @Schema(implementation = DailyStatView.class)),
//                            examples = @ExampleObject(value = """
//                [
//                  {"day": "2025-09-01", "bookings": 45, "cancellations": 3},
//                  {"day": "2025-09-02", "bookings": 52, "cancellations": 6}
//                ]
//                """)
//                    )
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid dates (format or range)"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid token)"),
//            @ApiResponse(responseCode = "403", description = "Forbidden (requires ROLE_ADMIN)"),
//            @ApiResponse(responseCode = "500", description = "Unexpected error")
//    })
//    @GetMapping("/daily-stats")
//    public ResponseEntity<List<DailyStatView>> daily(
//            @Parameter(
//                    name = "from",
//                    description = "Start date (inclusive), ISO-8601 format yyyy-MM-dd",
//                    example = "2025-09-01",
//                    in = ParameterIn.QUERY
//            )
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
//            @Parameter(
//                    name = "to",
//                    description = "End date (inclusive), ISO-8601 format yyyy-MM-dd",
//                    example = "2025-09-30",
//                    in = ParameterIn.QUERY
//            )
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
//    ) {
//        return ResponseEntity.ok(svc.daily(from, to));
//    }
}
