// src/main/java/atlan/evently/atlan/analytics/service/AdvancedAnalyticsService.java
package atlan.evently.atlan.analytics.service;

import atlan.evently.atlan.analytics.dto.*;
import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.booking.repo.BookingAnalyticsRepository;
import atlan.evently.atlan.booking.repo.BookingRepository;
import atlan.evently.atlan.event.repo.EventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdvancedAnalyticsService {

    private final EventRepository eventRepo;
    private final BookingRepository bookingRepo;

    public AdvancedAnalyticsService(EventRepository eventRepo, BookingRepository bookingRepo) {
        this.eventRepo = eventRepo;
        this.bookingRepo = bookingRepo;
    }

    @Transactional(readOnly = true)
    public AnalyticsTotals totals() {
        long totalEvents = eventRepo.countAllEvents();
        long totalBookings = bookingRepo.count();
        long confirmed = bookingRepo.countByStatus(Booking.Status.CONFIRMED);
        long cancelled = bookingRepo.countByStatus(Booking.Status.CANCELED);
        return new AnalyticsTotals(totalEvents, totalBookings, confirmed, cancelled);
    }

    @Transactional(readOnly = true)
    public List<MostBookedEventView> mostBooked(int limit) {
        return bookingRepo.topEvents(PageRequest.of(0, Math.max(limit, 1)));
    }

    @Transactional(readOnly = true)
    public List<CancellationRateView> cancellationRates() {
        return bookingRepo.cancellationRates();
    }

    @Transactional(readOnly = true)
    public List<DailyStatView> daily(LocalDate from, LocalDate to) {
        List<BookingAnalyticsRepository.DailyStatRow> rows = ((BookingAnalyticsRepository) bookingRepo).dailyStats(from, to);
        return rows.stream()
                .map(r -> new DailyStatView(r.getDay(), r.getBookings(), r.getCancellations()))
                .toList();
    }
}
