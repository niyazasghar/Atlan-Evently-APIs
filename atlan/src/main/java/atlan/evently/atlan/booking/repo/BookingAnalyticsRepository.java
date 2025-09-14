// src/main/java/atlan/evently/atlan/booking/repo/BookingAnalyticsRepository.java
package atlan.evently.atlan.booking.repo;

import atlan.evently.atlan.analytics.dto.MostBookedEventView;
import atlan.evently.atlan.analytics.dto.CancellationRateView;
import atlan.evently.atlan.analytics.dto.DailyStatView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingAnalyticsRepository {

    // Top events by confirmed bookings (JPQL constructor projection)
    @Query("""
        select new atlan.evently.atlan.analytics.dto.MostBookedEventView(
            e.id, e.name, count(b)
        )
        from Booking b join b.event e
        where b.status = atlan.evently.atlan.booking.model.Booking.Status.CONFIRMED
        group by e.id, e.name
        order by count(b) desc
    """)
    List<MostBookedEventView> topEvents(Pageable pageable);

    // Cancellation rates by event (JPQL with CASE expressions)
    @Query("""
        select new atlan.evently.atlan.analytics.dto.CancellationRateView(
            e.id,
            e.name,
            sum(case when b.status = atlan.evently.atlan.booking.model.Booking.Status.CONFIRMED then 1 else 0 end),
            sum(case when b.status = atlan.evently.atlan.booking.model.Booking.Status.CANCELED  then 1 else 0 end),
            (case when count(b) > 0
                  then (sum(case when b.status = atlan.evently.atlan.booking.model.Booking.Status.CANCELED then 1 else 0 end) * 100.0) / count(b)
                  else 0 end)
        )
        from Booking b join b.event e
        group by e.id, e.name
        order by 5 desc
    """)
    List<CancellationRateView> cancellationRates();

    // Daily stats (native SQL for date bucketing; Postgres shown)
    @Query(value = """
        select
          cast(b.booked_at as date) as day,
          sum(case when b.status = 'CONFIRMED' then 1 else 0 end) as bookings,
          sum(case when b.status = 'CANCELED'  then 1 else 0 end) as cancellations
        from bookings b
        where cast(b.booked_at as date) between :from and :to
        group by cast(b.booked_at as date)
        order by day asc
    """, nativeQuery = true)
    List<DailyStatRow> dailyStats(@Param("from") LocalDate from, @Param("to") LocalDate to);

    interface DailyStatRow {
        LocalDate getDay();
        long getBookings();
        long getCancellations();
    }
}
