// src/main/java/atlan/evently/atlan/booking/web/mapper/BookingMapper.java
package atlan.evently.atlan.booking.web;

import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.booking.web.dto.BookingResponse;
import java.util.List;
import java.util.stream.Collectors;

public final class BookingMapper {
    private BookingMapper() {}

    public static BookingResponse toResponse(Booking b) {
        BookingResponse r = new BookingResponse();
        r.setId(b.getId());
        // NEW: set userId from @ManyToOne User association (null-safe)
        r.setUserId(b.getUser() != null ? b.getUser().getId() : null);
        r.setEventId(b.getEvent() != null ? b.getEvent().getId() : null);
        r.setStatus(b.getStatus().name());
        r.setBookedAt(b.getBookedAt());
        r.setCanceledAt(b.getCanceledAt());
        return r;
    }

    public static List<BookingResponse> toResponseList(List<Booking> list) {
        return list.stream().map(BookingMapper::toResponse).collect(Collectors.toList());
    }
}
