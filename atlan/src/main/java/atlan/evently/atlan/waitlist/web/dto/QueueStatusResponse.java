// src/main/java/atlan/evently/atlan/waitlist/web/dto/QueueStatusResponse.java
package atlan.evently.atlan.waitlist.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
@Data
@Schema(description = "Queue position and ETA for the authenticated user on a specific event")
public class QueueStatusResponse {

    @Schema(example = "77", description = "Event ID")
    private Long eventId;

    @Schema(example = "AI Summit", description = "Event name")
    private String eventName;

    @Schema(example = "3", description = "1-based position in the event's waitlist")
    private Integer position;

    @Schema(example = "18", description = "Total number of people in the waitlist")
    private Integer total;

    @Schema(example = "2025-09-14T16:40:00Z", description = "Time the user joined the waitlist")
    private OffsetDateTime enqueuedAt;

    @Schema(example = "30", description = "Rough ETA in minutes until promotion (heuristic)")
    private Integer estimatedMinutes;

    // getters and setters...
}
