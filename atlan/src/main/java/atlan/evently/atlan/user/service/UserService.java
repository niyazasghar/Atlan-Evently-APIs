// src/main/java/atlan/evently/atlan/user/service/UserService.java
package atlan.evently.atlan.user.service;

import atlan.evently.atlan.user.model.User;
import atlan.evently.atlan.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User register(String email, String passwordHash, User.Role role) {
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash); // hash at controller/service edge before save in real apps
        user.setRole(role);
        return users.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return users.findByEmail(email);
    }

    public User getById(Long id) {
        return users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
