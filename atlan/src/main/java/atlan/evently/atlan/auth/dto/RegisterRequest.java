package atlan.evently.atlan.auth.dto;

// src/main/java/atlan/evently/atlan/auth/web/RegisterRequest.java


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    private String password;

    private String role = "USER";
}
