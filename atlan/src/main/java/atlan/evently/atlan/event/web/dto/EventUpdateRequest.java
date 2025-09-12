package atlan.evently.atlan.event.web.dto;

// src/main/java/com/evently/event/web/dto/EventUpdateRequest.java
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.OffsetDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventUpdateRequest {
    @NotBlank @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 200)
    private String venue;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private OffsetDateTime endTime;

    @Min(0)
    private int capacity;

    // getters and setters
}

