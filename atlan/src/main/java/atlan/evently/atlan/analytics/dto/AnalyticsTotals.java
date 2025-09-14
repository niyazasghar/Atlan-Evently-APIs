// src/main/java/atlan/evently/atlan/analytics/dto/AnalyticsTotals.java
package atlan.evently.atlan.analytics.dto;

public record AnalyticsTotals(
        long totalEvents,
        long totalBookings,
        long confirmedBookings,
        long cancelledBookings
) {}
