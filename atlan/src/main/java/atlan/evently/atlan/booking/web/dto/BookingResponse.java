package atlan.evently.atlan.booking.web.dto;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long eventId;
    private String status;
    private OffsetDateTime bookedAt;
    private OffsetDateTime canceledAt;
}
