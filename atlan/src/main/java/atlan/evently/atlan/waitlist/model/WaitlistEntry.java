// src/main/java/atlan/evently/atlan/waitlist/model/WaitlistEntry.java
package atlan.evently.atlan.waitlist.model;

import atlan.evently.atlan.event.model.Event;
import atlan.evently.atlan.user.model.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "waitlist",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_waitlist_event_user", columnNames = {"event_id","user_id"})
        },
        indexes = {
                @Index(name = "ix_waitlist_event_enqueued", columnList = "event_id,enqueued_at"),
                @Index(name = "ix_waitlist_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_waitlist_event"))
    private Event event;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_waitlist_user"))
    private User user;

    @Column(name = "enqueued_at", nullable = false, updatable = false)
    private OffsetDateTime enqueuedAt = OffsetDateTime.now();
}
