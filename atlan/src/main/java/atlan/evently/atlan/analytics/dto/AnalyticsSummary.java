// src/main/java/atlan/evently/atlan/analytics/dto/AnalyticsSummary.java
package atlan.evently.atlan.analytics.dto;

public record AnalyticsSummary(
        long totalEvents,
        long totalBookings,
        long confirmedBookings,
        long cancelledBookings,
        double globalUtilizationPercent
) {}
