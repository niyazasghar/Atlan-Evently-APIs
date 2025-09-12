package atlan.evently.atlan.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// src/main/java/com/evently/analytics/web/dto/EventStatsResponse.java
@AllArgsConstructor@NoArgsConstructor@Data
public class EventStatsResponse {
    private Long eventId;
    private String eventName;
    private int capacity;
    private long totalBookings;
    private double capacityUtilization;

    // getters and setters
}

