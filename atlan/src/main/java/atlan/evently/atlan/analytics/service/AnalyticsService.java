package atlan.evently.atlan.analytics.service;

// src/main/java/com/evently/analytics/service/AnalyticsService.java

import java.util.List;
import java.util.stream.Collectors;

import atlan.evently.atlan.analytics.view.EventStatsView;
import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.booking.repo.BookingRepository;
import atlan.evently.atlan.event.repo.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final EventRepository events;
    private final BookingRepository bookings;

    public AnalyticsService(EventRepository events, BookingRepository bookings) {
        this.events = events;
        this.bookings = bookings;
    }

    public List<EventStatsView> getEventStats() {
        // Simple in-DB count per event; for many events, prefer a single aggregate query or a view
        return events.findAll().stream().map(e -> {
            long confirmed = bookings.countByEvent_IdAndStatus(e.getId(), Booking.Status.CONFIRMED);
            double utilization = e.getCapacity() == 0 ? 0.0 : (confirmed * 100.0) / e.getCapacity();
            return new EventStatsView(e.getId(), e.getName(), e.getCapacity(), confirmed, utilization);
        }).collect(Collectors.toList());
    }
}
