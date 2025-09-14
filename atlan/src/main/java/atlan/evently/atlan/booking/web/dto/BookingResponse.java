// src/main/java/atlan/evently/atlan/booking/web/dto/BookingResponse.java
package atlan.evently.atlan.booking.web.dto;

import lombok.Data;

import java.time.OffsetDateTime;
@Data
public class BookingResponse {
    private Long id;
    private Long userId;     // NEW
    private Long eventId;
    private String status;
    private OffsetDateTime bookedAt;
    private OffsetDateTime canceledAt;

    // getters and setters...
}
