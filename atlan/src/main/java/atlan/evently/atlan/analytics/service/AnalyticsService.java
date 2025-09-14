package atlan.evently.atlan.analytics.service;

// src/main/java/atlan/evently/atlan/analytics/service/AnalyticsService.java

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import atlan.evently.atlan.analytics.view.EventStatsView;
import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.booking.repo.BookingRepository;
import atlan.evently.atlan.event.model.Event;
import atlan.evently.atlan.event.repo.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private final EventRepository events;
    private final BookingRepository bookings;

    public AnalyticsService(EventRepository events, BookingRepository bookings) {
        this.events = events;
        this.bookings = bookings;
    }

    // Existing behavior: compute per-event stats (N+1 counts)
    @Transactional(readOnly = true)
    public List<EventStatsView> getEventStats() {
        return events.findAll().stream().map(e -> {
            long confirmed = bookings.countByEvent_IdAndStatus(e.getId(), Booking.Status.CONFIRMED);
            double utilization = e.getCapacity() == 0 ? 0.0 : (confirmed * 100.0) / e.getCapacity();
            return new EventStatsView(e.getId(), e.getName(), e.getCapacity(), confirmed, utilization);
        }).collect(Collectors.toList());
    }

    // Overall summary across all events and bookings
    @Transactional(readOnly = true)
    public AnalyticsSummary getSummary() {
        List<Event> allEvents = events.findAll();
        long totalEvents = allEvents.size();

        long sumCapacity = allEvents.stream()
                .map(Event::getCapacity)
                .filter(c -> c != null)
                .mapToLong(Integer::longValue)
                .sum();

        long totalBookings = bookings.count();
        long confirmed = allEvents.stream()
                .map(Event::getId)
                .mapToLong(id -> bookings.countByEvent_IdAndStatus(id, Booking.Status.CONFIRMED))
                .sum();
        long cancelled = allEvents.stream()
                .map(Event::getId)
                .mapToLong(id -> bookings.countByEvent_IdAndStatus(id, Booking.Status.CANCELED))
                .sum();

        double globalUtil = sumCapacity > 0 ? (confirmed * 100.0) / sumCapacity : 0.0;

        return new AnalyticsSummary(totalEvents, totalBookings, confirmed, cancelled, globalUtil);
    }

    // Top-N events by confirmed bookings (uses totalBookings field/getter)
    @Transactional(readOnly = true)
    public List<EventStatsView> getPopularEventStats(int limit) {
        List<EventStatsView> all = getEventStats();
        return all.stream()
                .sorted(Comparator.comparingLong(EventStatsView::getTotalBookings).reversed())
                .limit(Math.max(limit, 1))
                .collect(Collectors.toList());
    }

    // DTO for summary response (kept inside service to avoid extra files)
    public static final class AnalyticsSummary {
        private final long totalEvents;
        private final long totalBookings;
        private final long confirmedBookings;
        private final long cancelledBookings;
        private final double globalUtilizationPercent;

        public AnalyticsSummary(long totalEvents, long totalBookings, long confirmedBookings,
                                long cancelledBookings, double globalUtilizationPercent) {
            this.totalEvents = totalEvents;
            this.totalBookings = totalBookings;
            this.confirmedBookings = confirmedBookings;
            this.cancelledBookings = cancelledBookings;
            this.globalUtilizationPercent = globalUtilizationPercent;
        }

        public long getTotalEvents() { return totalEvents; }
        public long getTotalBookings() { return totalBookings; }
        public long getConfirmedBookings() { return confirmedBookings; }
        public long getCancelledBookings() { return cancelledBookings; }
        public double getGlobalUtilizationPercent() { return globalUtilizationPercent; }
    }
}
