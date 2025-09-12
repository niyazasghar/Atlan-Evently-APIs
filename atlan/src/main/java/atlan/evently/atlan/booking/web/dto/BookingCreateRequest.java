package atlan.evently.atlan.booking.web.dto;

// src/main/java/com/evently/booking/web/dto/BookingCreateRequest.java


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingCreateRequest {
    @NotNull private Long userId;
    @NotNull private Long eventId;
}
