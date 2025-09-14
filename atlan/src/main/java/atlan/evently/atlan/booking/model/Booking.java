package atlan.evently.atlan.booking.model;

import atlan.evently.atlan.user.model.User;
import atlan.evently.atlan.event.model.Event;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_bookings_event_id", columnList = "event_id"),
                @Index(name = "idx_bookings_user_id",  columnList = "user_id")
        }
)
@NoArgsConstructor
@Getter
@Setter
public class Booking {

    public enum Status { CONFIRMED, CANCELED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookings_user"))
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookings_event"))
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Column(name = "booked_at", nullable = false)
    private OffsetDateTime bookedAt;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @PrePersist
    void onCreate() {
        if (bookedAt == null) bookedAt = OffsetDateTime.now();
        if (status == null) status = Status.CONFIRMED;
    }
}
