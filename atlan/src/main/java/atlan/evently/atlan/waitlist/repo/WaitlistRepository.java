// src/main/java/atlan/evently/atlan/waitlist/repo/WaitlistRepository.java
package atlan.evently.atlan.waitlist.repo;

import atlan.evently.atlan.waitlist.model.WaitlistEntry;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);

    List<WaitlistEntry> findByUser_IdOrderByEnqueuedAtAsc(Long userId);

    long countByEvent_Id(Long eventId);

    // Fetch user’s entries with event pre-fetched (helps mapping to DTOs cleanly)
    @Query("""
       select w from WaitlistEntry w
       join fetch w.event e
       where w.user.id = :userId
       order by w.enqueuedAt asc, w.id asc
    """)
    List<WaitlistEntry> findByUserIdFetchEventOrderByEnqueued(@Param("userId") Long userId);

    // Compute queue position: count entries strictly before (time) or tie-broken by id
    @Query("""
       select count(w) from WaitlistEntry w
       where w.event.id = :eventId
         and (w.enqueuedAt < :ts or (w.enqueuedAt = :ts and w.id < :id))
    """)
    long countAhead(@Param("eventId") Long eventId,
                    @Param("ts") OffsetDateTime enqueuedAt,
                    @Param("id") Long id);

    // Earliest row for an event, locked for update to avoid double-promotion under concurrency
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WaitlistEntry> findTopByEvent_IdOrderByEnqueuedAtAscIdAsc(Long eventId);
}
