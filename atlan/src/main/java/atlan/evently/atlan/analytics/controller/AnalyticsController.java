package atlan.evently.atlan.analytics.controller;

// src/main/java/atlan/evently/atlan/analytics/controller/AnalyticsController.java

import java.util.List;

import atlan.evently.atlan.analytics.service.AnalyticsService;
import atlan.evently.atlan.analytics.service.AnalyticsService.AnalyticsSummary;
import atlan.evently.atlan.analytics.view.EventStatsView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsService analytics;

    public AnalyticsController(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    // Existing endpoint: per-event stats (capacity, confirmed, utilization)
    @GetMapping("/events")
    public ResponseEntity<List<EventStatsView>> eventStats() {
        return ResponseEntity.ok(analytics.getEventStats());
    }

    // New: overall summary (events, bookings, confirmed, cancelled, global utilization)
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummary> summary() {
        return ResponseEntity.ok(analytics.getSummary());
    }

    // New: top-N events by confirmed bookings (defaults to 10)
    @GetMapping("/popular")
    public ResponseEntity<List<EventStatsView>> popular(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(analytics.getPopularEventStats(Math.max(limit, 1)));
    }
}
