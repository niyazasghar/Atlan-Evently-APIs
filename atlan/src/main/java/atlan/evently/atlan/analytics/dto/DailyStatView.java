// src/main/java/atlan/evently/atlan/analytics/dto/DailyStatView.java
package atlan.evently.atlan.analytics.dto;

import java.time.LocalDate;

public record DailyStatView(
        LocalDate day,
        long bookings,
        long cancellations
) {}
