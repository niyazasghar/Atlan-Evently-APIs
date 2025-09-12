package atlan.evently.atlan.auth.dto;

// src/main/java/atlan/evently/atlan/auth/web/AuthResponse.java

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
}
