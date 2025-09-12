package atlan.evently.atlan.booking.web.dto;

// src/main/java/atlan/evently/atlan/booking/web/dto/BookingResponse.java


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

