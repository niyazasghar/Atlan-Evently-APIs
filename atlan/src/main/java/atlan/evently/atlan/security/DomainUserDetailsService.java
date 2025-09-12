package atlan.evently.atlan.security;

// src/main/java/atlan/evently/atlan/security/DomainUserDetailsService.java

import atlan.evently.atlan.user.model.User;
import atlan.evently.atlan.user.repo.UserRepository;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class DomainUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public DomainUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = users.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Map domain role to ROLE_* authority
        String roleName = "ROLE_" + u.getRole().name();
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(roleName)))
                .accountExpired(false).accountLocked(false)
                .credentialsExpired(false).disabled(false)
                .build();
    }
}
