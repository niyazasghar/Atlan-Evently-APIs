package atlan.evently.atlan.event.web;
// src/main/java/atlan/evently/atlan/event/web/mapper/EventMapper.java

import atlan.evently.atlan.event.model.Event;
import atlan.evently.atlan.event.web.dto.EventResponse;
import java.util.List;
import java.util.stream.Collectors;

public final class EventMapper {
    private EventMapper() {}

    public static EventResponse toResponse(Event e) {
        EventResponse r = new EventResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setVenue(e.getVenue());
        r.setStartTime(e.getStartTime());
        r.setEndTime(e.getEndTime());
        r.setCapacity(e.getCapacity());
        return r;
    }

    public static List<EventResponse> toResponseList(List<Event> events) {
        return events.stream().map(EventMapper::toResponse).collect(Collectors.toList());
    }
}
