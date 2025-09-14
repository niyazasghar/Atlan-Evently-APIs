// src/main/java/atlan/evently/atlan/waitlist/web/dto/QueueStatusResponse.java
package atlan.evently.atlan.waitlist.web.dto;

import java.time.OffsetDateTime;

public class QueueStatusResponse {
    private Long eventId;
    private String eventName;
    private Integer position;     // 1-based
    private Integer total;
    private OffsetDateTime enqueuedAt;
    private Integer estimatedMinutes; // simplistic estimate; optional

    // getters/setters omitted for brevity
}
