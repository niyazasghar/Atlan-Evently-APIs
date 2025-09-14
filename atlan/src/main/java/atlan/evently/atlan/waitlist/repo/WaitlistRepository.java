// src/main/java/atlan/evently/atlan/waitlist/repo/WaitlistRepository.java
package atlan.evently.atlan.waitlist.repo;

import atlan.evently.atlan.waitlist.model.WaitlistEntry;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);

    List<WaitlistEntry> findByUser_IdOrderByEnqueuedAtAsc(Long userId);

    long countByEvent_Id(Long eventId);

    // Earliest row for an event, locked for update to avoid double-promotion under concurrency
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WaitlistEntry> findTopByEvent_IdOrderByEnqueuedAtAscIdAsc(Long eventId);
}
