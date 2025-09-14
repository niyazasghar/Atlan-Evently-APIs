// src/main/java/atlan/evently/atlan/booking/repo/BookingAnalyticsRepository.java
package atlan.evently.atlan.booking.repo;

import atlan.evently.atlan.booking.model.Booking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingAnalyticsRepository extends Repository<Booking, Long> {

    interface DailyStatRow {
        LocalDate getDay();
        long getBookings();
        long getCancellations();
    }

    @Query(value = """
      with days as (
        select generate_series(:from::date, :to::date, interval '1 day')::date as day
      )
      select
        d.day as day,
        coalesce(sum(case when b.status = 'CONFIRMED' and b.booked_at::date  = d.day then 1 else 0 end), 0) as bookings,
        coalesce(sum(case when b.status = 'CANCELED'  and b.canceled_at::date = d.day then 1 else 0 end), 0) as cancellations
      from days d
      left join bookings b
        on (b.booked_at::date = d.day or b.canceled_at::date = d.day)
      group by d.day
      order by d.day
    """, nativeQuery = true)
    List<DailyStatRow> dailyStats(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
