package atlan.evently.atlan.security.jwt;
// src/main/java/atlan/evently/atlan/security/jwt/JwtAuthFilter.java


import atlan.evently.atlan.security.DomainUserDetailsService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthFilter extends org.springframework.web.filter.OncePerRequestFilter {

    private final JwtService jwtService;
    private final DomainUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, DomainUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception ex) {
            chain.doFilter(request, response); // invalid token -> proceed without auth
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, userDetails)) {
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
