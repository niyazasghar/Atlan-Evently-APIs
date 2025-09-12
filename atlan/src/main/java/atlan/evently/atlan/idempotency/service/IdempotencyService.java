// src/main/java/.../idempotency/service/IdempotencyService.java
package atlan.evently.atlan.idempotency.service;

import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.booking.repo.BookingRepository;
import atlan.evently.atlan.idempotency.model.IdempotencyRecord;
import atlan.evently.atlan.idempotency.repo.IdempotencyRecordRepository;
import atlan.evently.atlan.booking.web.BookingMapper;
import atlan.evently.atlan.booking.web.dto.BookingResponse;
import java.time.OffsetDateTime;
import java.util.function.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {
    private final IdempotencyRecordRepository repo;
    private final BookingRepository bookingRepo;

    public IdempotencyService(IdempotencyRecordRepository repo, BookingRepository bookingRepo) {
        this.repo = repo;
        this.bookingRepo = bookingRepo;
    }

    @Transactional
    public ResponseEntity<BookingResponse> executeCreateBooking(
            String key, String endpoint, Long userId, String requestHash,
            Supplier<Booking> action) {

        // First-wins insert; if 0 rows, someone already inserted this key
        int inserted = repo.tryInsert(key, endpoint, userId, requestHash, OffsetDateTime.now().plusDays(2));
        if (inserted == 0) {
            IdempotencyRecord existing = repo.findByIdempotencyKeyAndEndpoint(key, endpoint)
                    .orElseThrow(() -> new IllegalStateException("Idempotency record not found"));

            // Key reuse with different payload
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new IllegalStateException("Idempotency-Key reuse with different request (409)");
            }

            // Replay previous result
            return switch (existing.getStatus()) {
                case "SUCCESS" -> {
                    var booking = bookingRepo.findById(existing.getBookingId())
                            .orElse(null);
                    if (booking == null) {
                        // Fallback to stored response body/code if persisted
                        yield ResponseEntity.status(existing.getResponseCode() == null ? 200 : existing.getResponseCode())
                                .build();
                    }
                    yield ResponseEntity.status(existing.getResponseCode() == null ? 201 : existing.getResponseCode())
                            .body(BookingMapper.toResponse(booking));
                }
                case "FAILURE" -> ResponseEntity.status(existing.getResponseCode() == null ? 422 : existing.getResponseCode()).build();
                case "IN_PROGRESS" -> ResponseEntity.status(409).build(); // advise client to retry after a short delay
                default -> ResponseEntity.status(500).build();
            };
        }

        // First processor: perform the action and persist outcome
        try {
            Booking booking = action.get(); // executes capacity checks + optimistic locking
            IdempotencyRecord rec = repo.findByIdempotencyKeyAndEndpoint(key, endpoint).orElseThrow();
            rec.setStatus("SUCCESS");
            rec.setBookingId(booking.getId());
            rec.setResponseCode(201);
            // Optionally serialize the response body JSON to rec.setResponseBody(...)
            // repo.save(rec); // not needed if JPA tracks entity; call if detached
            return ResponseEntity.status(201).body(BookingMapper.toResponse(booking));
        } catch (RuntimeException ex) {
            IdempotencyRecord rec = repo.findByIdempotencyKeyAndEndpoint(key, endpoint).orElseThrow();
            rec.setStatus("FAILURE");
            rec.setResponseCode(409); // or 422 based on exception mapping
            // rec.setResponseBody(...optional error json...)
            throw ex;
        }
    }
}
