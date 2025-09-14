package atlan.evently.atlan.event.repo;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import atlan.evently.atlan.event.model.Event;
import jakarta.persistence.LockModeType;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    @Query("select e from Event e where e.startTime >= :now order by e.startTime asc")
    Page<Event> findUpcoming(OffsetDateTime now, Pageable pageable);
    @Query("select count(e) from Event e")
    long countAllEvents();

    @Query("select coalesce(sum(e.capacity),0) from Event e")
    long sumCapacity();
}
