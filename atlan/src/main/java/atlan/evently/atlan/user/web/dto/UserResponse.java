// src/main/java/atlan/evently/atlan/user/web/dto/UserResponse.java
package atlan.evently.atlan.user.web.dto;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String role;
    private OffsetDateTime createdAt;
}
