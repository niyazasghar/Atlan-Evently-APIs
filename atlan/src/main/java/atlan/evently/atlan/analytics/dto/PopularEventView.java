// src/main/java/atlan/evently/atlan/analytics/dto/PopularEventView.java
package atlan.evently.atlan.analytics.dto;

public record PopularEventView(
        Long eventId,
        String name,
        String venue,
        long confirmedCount,
        int capacity,
        double utilizationPercent
) {}
