package atlan.evently.atlan.idempotency.model;

// src/main/java/.../idempotency/model/IdempotencyRecord.java


import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "idempotency_records",
        indexes = @Index(name = "ux_idem_key_endpoint",
                columnList = "idempotency_key,endpoint", unique = true))
@Getter @Setter @NoArgsConstructor
public class IdempotencyRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="idempotency_key", nullable=false, length=80)
    private String idempotencyKey;

    @Column(nullable=false, length=120)
    private String endpoint;

    @Column(name="user_id")
    private Long userId;

    @Column(name="request_hash", nullable=false, length=64)
    private String requestHash;

    @Column(nullable=false, length=20)
    private String status; // IN_PROGRESS, SUCCESS, FAILURE

    @Column(name="response_code")
    private Integer responseCode;

    @Lob
    @Column(name="response_body")
    private String responseBody;

    @Column(name="booking_id")
    private Long bookingId;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @Column(name="expires_at", nullable=false)
    private OffsetDateTime expiresAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (expiresAt == null) expiresAt = createdAt.plusDays(2);
    }
}
