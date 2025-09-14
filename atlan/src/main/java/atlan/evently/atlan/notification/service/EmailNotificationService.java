// src/main/java/atlan/evently/atlan/notification/EmailNotificationService.java
package atlan.evently.atlan.notification;

import atlan.evently.atlan.booking.model.Booking;
import atlan.evently.atlan.event.model.Event;
import atlan.evently.atlan.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@evently.local}")
    private String from;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendWaitlistPromotion(User user, Event event, Booking booking) {
        if (user == null || user.getEmail() == null) return;

        String display = user.getEmail(); // safe fallback to email if no name field
        String subject = "Booking confirmed: " + event.getName();
        String body = """
            Hi %s,

            Great news! A spot opened up and your waitlist entry has been promoted to a confirmed booking.

            Event: %s
            Venue: %s
            Starts: %s
            Ends:   %s
            Booking ID: %d

            See you there!
            """.formatted(
                display,
                event.getName(),
                event.getVenue(),
                event.getStartTime(),
                event.getEndTime(),
                booking.getId()
        );

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(user.getEmail());
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}
