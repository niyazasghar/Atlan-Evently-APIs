//package atlan.evently.atlan.booking.it;
//
//import atlan.evently.atlan.booking.model.Booking;
//import atlan.evently.atlan.booking.repo.BookingRepository;
//import atlan.evently.atlan.booking.service.BookingService;
//import atlan.evently.atlan.event.model.Event;
//import atlan.evently.atlan.event.repo.EventRepository;
//import atlan.evently.atlan.user.model.User;
//import atlan.evently.atlan.user.service.UserService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.time.OffsetDateTime;
//import java.util.ArrayList;
//
//// src/test/java/.../booking/it/BookingCapacityIT.java
//@SpringBootTest
//@Testcontainers
//class BookingCapacityIT {
//
//    @Container
//    @org.springframework.boot.testcontainers.service.connection.ServiceConnection
//    static org.testcontainers.containers.PostgreSQLContainer<?> pg =
//            new org.testcontainers.containers.PostgreSQLContainer<>("postgres:16");
//
//    @Autowired
//    BookingService bookings;
//    @Autowired
//    UserService users;
//    @Autowired
//    EventRepository events;
//    @Autowired
//    BookingRepository bookingRepo;
//
//    @Test
//    void concurrent_bookings_do_not_exceed_capacity() throws Exception {
//        var now = OffsetDateTime.now();
//        var event = events.save(new Event(null, "E", "V", now.plusMinutes(30), now.plusHours(2), 5, null, 0));
//        // create N users
//        var ids = new ArrayList<Long>();
//        for (int i = 0; i < 20; i++) {
//            ids.add(users.register("u"+i+"@ex.com", "x", User.Role.USER).getId());
//        }
//        var latch = new java.util.concurrent.CountDownLatch(1);
//        var pool = java.util.concurrent.Executors.newFixedThreadPool(20);
//        for (Long uid: ids) {
//            pool.submit(() -> {
//                try {
//                    latch.await();
//                    try { bookings.createBooking(uid, event.getId()); } catch (Exception ignore) {}
//                } catch (InterruptedException ignored) {}
//            });
//        }
//        latch.countDown();
//        pool.shutdown();
//        pool.awaitTermination(20, java.util.concurrent.TimeUnit.SECONDS);
//
//        long confirmed = bookingRepo.countByEvent_IdAndStatus(event.getId(), Booking.Status.CONFIRMED);
//        org.assertj.core.api.Assertions.assertThat(confirmed).isEqualTo(5);
//    }
//}
