// src/main/java/atlan/evently/atlan/analytics/controller/AnalyticsController.java
package atlan.evently.atlan.analytics.controller;

import java.util.List;

import atlan.evently.atlan.analytics.service.AnalyticsService;
import atlan.evently.atlan.analytics.service.AnalyticsService.AnalyticsSummary;
import atlan.evently.atlan.analytics.view.EventStatsView;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(
        name = "Analytics",
        description = "Public analytics for events: per‑event stats, overall summary, and popular events"
)
@Validated
public class AnalyticsController {
    private final AnalyticsService analytics;

    public AnalyticsController(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    @Operation(
            summary = "Per‑event stats",
            description = "Returns capacity, confirmed bookings, and utilization percentage for each event."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event stats fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EventStatsView.class)),
                            examples = @ExampleObject(value = """
                [
                  {
                    "eventId": 101,
                    "eventName": "DevFest",
                    "capacity": 300,
                    "totalBookings": 180,
                    "capacityUtilization": 60.0
                  },
                  {
                    "eventId": 102,
                    "eventName": "Cloud Day",
                    "capacity": 150,
                    "totalBookings": 120,
                    "capacityUtilization": 80.0
                  }
                ]
                """)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/events")
    public ResponseEntity<List<EventStatsView>> eventStats() {
        return ResponseEntity.ok(analytics.getEventStats());
    }

    @Operation(
            summary = "Overall summary",
            description = "Returns totals across all events and bookings, including global capacity utilization."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Summary fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnalyticsSummary.class),
                            examples = @ExampleObject(value = """
                {
                  "totalEvents": 42,
                  "totalBookings": 1280,
                  "confirmedBookings": 1100,
                  "cancelledBookings": 180,
                  "globalUtilizationPercent": 54.3
                }
                """)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummary> summary() {
        return ResponseEntity.ok(analytics.getSummary());
    }

    @Operation(
            summary = "Popular events (Top‑N)",
            description = "Returns the top events sorted by confirmed bookings; limit controls the number of rows."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Popular events fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EventStatsView.class)),
                            examples = @ExampleObject(value = """
                [
                  {
                    "eventId": 201,
                    "eventName": "AI Summit",
                    "capacity": 500,
                    "totalBookings": 420,
                    "capacityUtilization": 84.0
                  },
                  {
                    "eventId": 105,
                    "eventName": "Kotlin Conf",
                    "capacity": 250,
                    "totalBookings": 210,
                    "capacityUtilization": 84.0
                  }
                ]
                """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid limit value"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/popular")
    public ResponseEntity<List<EventStatsView>> popular(
            @Parameter(
                    name = "limit",
                    description = "Maximum number of events to return (1–100). Defaults to 10.",
                    in = ParameterIn.QUERY,
                    example = "10"
            )
            @RequestParam(name = "limit", defaultValue = "10") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(analytics.getPopularEventStats(Math.max(limit, 1)));
    }
}
