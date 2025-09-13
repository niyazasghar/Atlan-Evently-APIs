//package atlan.evently.atlan.booking.service;
//
//
//import atlan.evently.atlan.booking.model.Booking;
//import atlan.evently.atlan.booking.repo.BookingRepository;
//import atlan.evently.atlan.event.model.Event;
//import atlan.evently.atlan.event.repo.EventRepository;
//import atlan.evently.atlan.user.model.User;
//import atlan.evently.atlan.user.service.UserService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//
//
//import java.util.Optional;
//
//import static javax.management.Query.times;
//
//import static org.hamcrest.Matchers.any;
//import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertThrows;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//// src/test/java/.../booking/service/BookingServiceTest.java
//@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
//class BookingServiceTest {
//
//    @Mock
//    BookingRepository bookings;
//    @Mock
//    EventRepository events;
//    @Mock
//    UserService users;
//
//    @InjectMocks
//    BookingService service;
//
//    @Test
//    void create_success() {
//        var user = new User(); user.setId(1L);
//        var event = new Event(); event.setId(10L); event.setCapacity(5);
//
//        when(users.getById(1L)).thenReturn(user);
//        when(events.findByIdForUpdate(10L)).thenReturn(Optional.of(event));
//        when(bookings.findByUser_IdAndEvent_IdAndStatus(1L, 10L, Booking.Status.CONFIRMED))
//                .thenReturn(Optional.empty());
//        when(bookings.countByEvent_IdAndStatus(10L, Booking.Status.CONFIRMED)).thenReturn(0L);
//        when(bookings.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        var b = service.createBooking(1L, 10L);
//        assertEquals(Booking.Status.CONFIRMED, b.getStatus());
//        verify(bookings).save(any());
//    }
//
//    @Test
//    void create_retries_on_optimistic_lock_up_to_3() {
//        var user = new User(); user.setId(1L);
//        var event = new Event(); event.setId(10L); event.setCapacity(5);
//
//        when(users.getById(1L)).thenReturn(user);
//        when(events.findByIdForUpdate(10L)).thenReturn(Optional.of(event));
//        when(bookings.findByUser_IdAndEvent_IdAndStatus(1L, 10L, Booking.Status.CONFIRMED))
//                .thenReturn(Optional.empty());
//        when(bookings.countByEvent_IdAndStatus(10L, Booking.Status.CONFIRMED)).thenReturn(0L);
//        when(bookings.save(any()))
//                .thenThrow(new jakarta.persistence.OptimisticLockException())
//                .thenThrow(new jakarta.persistence.OptimisticLockException())
//                .thenAnswer(inv -> inv.getArgument(0));
//
//        var b = service.createBooking(1L, 10L);
//        assertNotNull(b);
//        verify(bookings, times(3)).save(any());
//    }
//
//    @Test
//    void create_fails_when_capacity_full() {
//        var user = new User(); user.setId(1L);
//        var event = new Event(); event.setId(10L); event.setCapacity(1);
//
//        when(users.getById(1L)).thenReturn(user);
//        when(events.findByIdForUpdate(10L)).thenReturn(Optional.of(event));
//        when(bookings.findByUser_IdAndEvent_IdAndStatus(1L, 10L, Booking.Status.CONFIRMED))
//                .thenReturn(Optional.empty());
//        when(bookings.countByEvent_IdAndStatus(10L, Booking.Status.CONFIRMED)).thenReturn(1L);
//
//        assertThrows(IllegalStateException.class, () -> service.createBooking(1L, 10L));
//    }
//}
