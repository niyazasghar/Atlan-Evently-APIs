// src/main/java/atlan/evently/atlan/event/service/EventAdminService.java
package atlan.evently.atlan.event.service;

import atlan.evently.atlan.event.model.Event;
import atlan.evently.atlan.event.repo.EventRepository;
import atlan.evently.atlan.event.web.dto.EventCreateRequest;
import atlan.evently.atlan.event.web.dto.EventUpdateRequest;
import atlan.evently.atlan.event.web.dto.EventResponse;
import atlan.evently.atlan.event.web.EventMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-facing write operations for Events.
 * Evicts read caches on every write so cached list/detail views are refreshed immediately after changes.
 * Caches used:
 *  - "eventDetail" (key: event id)
 *  - "eventListUpcoming" (paged lists; evict all because keys vary by page/size/sort)
 */
@Service
public class EventAdminService {
    private final EventRepository repo;
    public EventAdminService(EventRepository repo) {
        this.repo = repo;
    }

    /**
     * Create a new Event, validating time bounds and evicting list cache to refresh listings.
     * Eviction strategy:
     *  - Clear all entries in "eventListUpcoming" because list keys vary and we need immediate freshness.
     */
    @Transactional
    @CacheEvict(cacheNames = {"eventListUpcoming"}, allEntries = true)
    public EventResponse create(EventCreateRequest req) {
        if (req.getEndTime().isBefore(req.getStartTime())) {
            throw new IllegalArgumentException("end_time must be after start_time");
        }
        Event e = new Event();
        e.setName(req.getName());
        e.setVenue(req.getVenue());
        e.setStartTime(req.getStartTime());
        e.setEndTime(req.getEndTime());
        e.setCapacity(req.getCapacity());
        Event saved = repo.save(e);
        return EventMapper.toResponse(saved);
    }

    /**
     * Update an existing Event using optimistic locking (version increments on commit).
     * Eviction strategy:
     *  - Remove the "eventDetail" cache entry for this id.
     *  - Clear all entries in "eventListUpcoming" because list pages are keyed differently.
     */
    @Transactional
    @CacheEvict(cacheNames = {"eventDetail", "eventListUpcoming"}, key = "#id", allEntries = true)
    public EventResponse update(Long id, EventUpdateRequest req) {
        Event e = repo.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (req.getEndTime().isBefore(req.getStartTime())) {
            throw new IllegalArgumentException("end_time must be after start_time");
        }
        e.setName(req.getName());
        e.setVenue(req.getVenue());
        e.setStartTime(req.getStartTime());
        e.setEndTime(req.getEndTime());
        e.setCapacity(req.getCapacity());
        // no explicit save needed; JPA dirty checking flushes on commit
        return EventMapper.toResponse(e);
    }

    /**
     * Delete an Event and evict related caches so stale data isn't served.
     * Eviction strategy mirrors update: drop detail for id and all list entries.
     */
    @Transactional
    @CacheEvict(cacheNames = {"eventDetail", "eventListUpcoming"}, key = "#id", allEntries = true)
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Event not found");
        }
        repo.deleteById(id);
    }
}
