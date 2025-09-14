// src/main/java/atlan/evently/atlan/analytics/dto/CancellationRateView.java
package atlan.evently.atlan.analytics.dto;

public record CancellationRateView(
        Long eventId,
        String eventName,
        long confirmedCount,
        long cancelledCount,
        double cancellationRatePercent
) {}
