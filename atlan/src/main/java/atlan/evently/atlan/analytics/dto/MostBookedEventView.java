// src/main/java/atlan/evently/atlan/analytics/dto/MostBookedEventView.java
package atlan.evently.atlan.analytics.dto;

public record MostBookedEventView(
        Long eventId,
        String eventName,
        long confirmedCount
) {}
