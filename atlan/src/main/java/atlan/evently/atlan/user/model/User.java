// src/main/java/atlan/evently/atlan/user/model/User.java
package atlan.evently.atlan.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(name = "ux_users_email", columnNames = "email"))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    public enum Role { USER, ADMIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
