// src/main/java/atlan/evently/atlan/analytics/controller/AdvancedAnalyticsController.java
package atlan.evently.atlan.analytics.controller;

import atlan.evently.atlan.analytics.dto.*;
import atlan.evently.atlan.analytics.service.AdvancedAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/analytics")
public class AdvancedAnalyticsController {

    private final AdvancedAnalyticsService svc;

    public AdvancedAnalyticsController(AdvancedAnalyticsService svc) {
        this.svc = svc;
    }

    @GetMapping("/totals")
    public ResponseEntity<AnalyticsTotals> totals() {
        return ResponseEntity.ok(svc.totals());
    }

    @GetMapping("/most-booked")
    public ResponseEntity<List<MostBookedEventView>> mostBooked(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(svc.mostBooked(limit));
    }

    @GetMapping("/cancellation-rates")
    public ResponseEntity<List<CancellationRateView>> cancellationRates() {
        return ResponseEntity.ok(svc.cancellationRates());
    }

    @GetMapping("/daily-stats")
    public ResponseEntity<List<DailyStatView>> daily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(svc.daily(from, to));
    }
}
