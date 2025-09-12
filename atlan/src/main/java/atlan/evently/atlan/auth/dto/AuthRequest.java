package atlan.evently.atlan.auth.dto;// src/main/java/.../auth/dto/AuthRequest.java
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @Schema(example = "asghar@example.com")
    @Email @NotBlank
    private String email;

    @Schema(example = "niyaz@example123")
    @NotBlank
    private String password;
}
