// src/main/java/atlan/evently/atlan/event/service/EventService.java
package atlan.evently.atlan.event.service;

import atlan.evently.atlan.event.model.Event;
import atlan.evently.atlan.event.repo.EventRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EventService
 * - Caches read-heavy methods (detail and upcoming list) to reduce DB load and latency.
 * - Evicts caches on writes (create/update) to keep results fresh immediately after changes.
 *
 * Cache names used:
 * - "eventDetail" -> single event by id
 * - "eventListUpcoming" -> paged upcoming events
 *
 * Requires a CacheManager bean and @EnableCaching in configuration (e.g., Caffeine or Redis).
 */
@Service
public class EventService {
    private final EventRepository events;
    public EventService(EventRepository events) {
        this.events = events;
    }

    /**
     * Create a new event.
     * Evicts all "eventListUpcoming" entries because pagination keys vary and the list must refresh.
     */
    @Transactional
    @CacheEvict(cacheNames = {"eventListUpcoming"}, allEntries = true)
    public Event create(String name, String venue, OffsetDateTime start, OffsetDateTime end, int capacity) {
        if (end.isBefore(start)) throw new IllegalArgumentException("end_time must be after start_time");
        Event e = new Event();
        e.setName(name);
        e.setVenue(venue);
        e.setStartTime(start);
        e.setEndTime(end);
        e.setCapacity(capacity);
        return events.save(e);
    }

    /**
     * Update an existing event using optimistic locking.
     * Evicts the "eventDetail" cache for this id and clears "eventListUpcoming" to refresh lists everywhere.
     */
    @Transactional
    @CacheEvict(cacheNames = {"eventDetail", "eventListUpcoming"}, key = "#id", allEntries = true)
    public Event update(Long id, String name, String venue, OffsetDateTime start, OffsetDateTime end, int capacity) {
        Event e = events.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (end.isBefore(start)) throw new IllegalArgumentException("end_time must be after start_time");
        e.setName(name);
        e.setVenue(venue);
        e.setStartTime(start);
        e.setEndTime(end);
        e.setCapacity(capacity);
        // Entity is dirty and will be flushed/committed; version increments on commit
        return e;
    }


    /**
     * List upcoming events (paged).
     * Cache key includes page and size; keep TTL short in CacheManager and evict on writes.
     * Note: Returning List<Event> from a Page requires .getContent().
     */
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "eventListUpcoming",
            key = "T(java.util.Objects).hash(#page,#size)", // include all inputs that affect result
            // unless = "#result == null",  <-- REMOVED THIS LINE
            sync = true
    )
    public List<Event> listUpcoming(int page, int size) {
        return events.findUpcoming(OffsetDateTime.now(), PageRequest.of(page, size)).getContent();
    }

    /**
     * Get single event by id (detail).
     * Cache by id; do not cache nulls to avoid masking not-found cases.
     */
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "eventDetail",
            key = "#id",
            // unless = "#result == null",  <-- REMOVED THIS LINE
            sync = true
    )
    public Event get(Long id) {
        return events.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }
}