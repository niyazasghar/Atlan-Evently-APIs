// src/main/java/atlan/evently/atlan/event/repo/EventAnalyticsRepository.java
package atlan.evently.atlan.event.repo;

import org.springframework.data.jpa.repository.Query;

public interface EventAnalyticsRepository {

    @Query("select count(e) from Event e")
    long countAllEvents();

    @Query("select coalesce(sum(e.capacity),0) from Event e")
    long sumCapacity();
}
