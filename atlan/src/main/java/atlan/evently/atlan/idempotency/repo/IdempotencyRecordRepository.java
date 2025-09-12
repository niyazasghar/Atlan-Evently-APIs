// src/main/java/.../idempotency/repo/IdempotencyRecordRepository.java
package atlan.evently.atlan.idempotency.repo;

import atlan.evently.atlan.idempotency.model.IdempotencyRecord;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKeyAndEndpoint(String key, String endpoint);

    @Modifying
    @Transactional
    @Query(value = """
      INSERT INTO idempotency_records
        (idempotency_key, endpoint, user_id, request_hash, status, created_at, expires_at)
      VALUES (:key, :endpoint, :userId, :hash, 'IN_PROGRESS', NOW(), :expiresAt)
      ON CONFLICT (idempotency_key, endpoint) DO NOTHING
    """, nativeQuery = true)
    int tryInsert(@Param("key") String key,
                  @Param("endpoint") String endpoint,
                  @Param("userId") Long userId,
                  @Param("hash") String hash,
                  @Param("expiresAt") OffsetDateTime expiresAt);
}
