// src/main/java/atlan/evently/atlan/user/repo/UserRepository.java
package atlan.evently.atlan.user.repo;

import atlan.evently.atlan.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
