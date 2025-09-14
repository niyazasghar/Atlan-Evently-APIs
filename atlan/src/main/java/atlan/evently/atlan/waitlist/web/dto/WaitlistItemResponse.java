// src/main/java/atlan/evently/atlan/waitlist/web/dto/WaitlistItemResponse.java
package atlan.evently.atlan.waitlist.web.dto;

import java.time.OffsetDateTime;

public class WaitlistItemResponse {
    private Long id;
    private Long eventId;
    private String eventName;
    private OffsetDateTime enqueuedAt;
    private Integer position; // 1-based in user's queue

    public WaitlistItemResponse() {}
    public WaitlistItemResponse(Long id, Long eventId, String eventName, OffsetDateTime enqueuedAt, Integer position) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.enqueuedAt = enqueuedAt;
        this.position = position;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public OffsetDateTime getEnqueuedAt() { return enqueuedAt; }
    public void setEnqueuedAt(OffsetDateTime enqueuedAt) { this.enqueuedAt = enqueuedAt; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
