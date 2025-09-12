// src/main/java/atlan/evently/atlan/user/web/dto/UserRegisterRequest.java
package atlan.evently.atlan.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    private String role = "USER";
}
