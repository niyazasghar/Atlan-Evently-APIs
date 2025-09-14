package atlan.evently.atlan.booking.repo;

import atlan.evently.atlan.analytics.dto.CancellationRateView;
import atlan.evently.atlan.analytics.dto.MostBookedEventView;
import atlan.evently.atlan.analytics.dto.PopularEventView;
import atlan.evently.atlan.analytics.pro.VenueAggregateProjection;
import atlan.evently.atlan.booking.model.Booking;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByEvent_IdAndStatus(Long eventId, Booking.Status status);

    Optional<Booking> findByUser_IdAndEvent_IdAndStatus(Long userId, Long eventId, Booking.Status status);

    java.util.List<Booking> findByUser_IdOrderByBookedAtDesc(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b join fetch b.event where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b join fetch b.event where b.id = :id and b.user.id = :userId")
    Optional<Booking> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);
    @Query("select count(b) from Booking b where b.status = atlan.evently.atlan.booking.model.Booking.Status.CONFIRMED")
    long countAllConfirmed();

    @Query("select count(b) from Booking b where b.status = atlan.evently.atlan.booking.model.Booking.Status.CANCELED")
    long countAllCancelled();

    // Top events by confirmed bookings (DTO constructor expression)
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
    List<MostBookedEventView> topEvents(org.springframework.data.domain.Pageable pageable);

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

    long countByStatus(Booking.Status status);

}
