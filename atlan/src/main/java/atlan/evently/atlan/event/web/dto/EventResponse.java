package atlan.evently.atlan.event.web.dto;

// src/main/java/com/evently/event/web/dto/EventResponse.java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventResponse {
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    private Long id;
    private String name;
    private String venue;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private int capacity;

    // getters and setters
}
