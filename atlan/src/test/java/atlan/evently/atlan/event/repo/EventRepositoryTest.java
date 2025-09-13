//package atlan.evently.atlan.event.repo;// src/test/java/.../event/repo/EventRepositoryTest.java
//
//import atlan.evently.atlan.event.model.Event;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.PageRequest;
//
//import java.time.OffsetDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//class EventRepositoryTest {
//
//    @Autowired
//    EventRepository repo;
//
//    @Test
//    void findUpcoming_returns_sorted_paged() {
//        var now = OffsetDateTime.now();
//        // seed some rows
//        repo.save(new Event(null, "A", "V", now.plusHours(1), now.plusHours(2), 10, null, 0));
//        repo.save(new Event(null, "B", "V", now.plusHours(3), now.plusHours(4), 10, null, 0));
//        repo.save(new Event(null, "C", "V", now.minusHours(2), now.minusHours(1), 10, null, 0)); // past
//
//        var page = repo.findUpcoming(now, PageRequest.of(0, 10));
//        assertThat(page.getTotalElements()).isEqualTo(2);
//        assertThat(page.getContent().get(0).getStartTime())
//                .isBefore(page.getContent().get(1).getStartTime());
//    }
//}
