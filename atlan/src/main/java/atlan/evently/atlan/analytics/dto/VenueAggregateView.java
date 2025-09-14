// src/main/java/atlan/evently/atlan/analytics/dto/VenueAggregateView.java
package atlan.evently.atlan.analytics.dto;

public record VenueAggregateView(
        String venue,
        long confirmedCount,
        long cancelledCount,
        int totalCapacity,
        double utilizationPercent
) {}
