package atlan.evently.atlan.analytics.controller;

// src/main/java/com/evently/analytics/web/AnalyticsController.java

import java.util.List;

import atlan.evently.atlan.analytics.service.AnalyticsService;
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

    @GetMapping("/events")
    public ResponseEntity<List<EventStatsView>> eventStats() {
        return ResponseEntity.ok(analytics.getEventStats());
    }
}
