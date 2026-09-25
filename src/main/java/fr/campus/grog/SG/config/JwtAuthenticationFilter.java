package fr.campus.grog.SG.config;

import fr.campus.grog.SG.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Filter that executes once per request to intercept and validate JWT Bearer tokens.
 * If a valid token is present in the Authorization header, it populates the SecurityContextHolder.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract the Authorization header
        String authHeader = request.getHeader("Authorization");

        // If header is missing or does not start with "Bearer ", proceed to next filter in the chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2: Strip the "Bearer " prefix (7 characters) to obtain raw compact JWT
        String token = authHeader.substring(7);

        // Step 3: Validate the cryptographic signature and expiration
        if (this.jwtService.isTokenValid(token)) {
            String username = this.jwtService.extractUsername(token);

            UUID userId = this.jwtService.extractUserId(token);
            // Step 4: If subject is valid and security context has no active authentication yet
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                //UserDetails userDetails = this.userDetailsService.loadUserByUsername(username); //used in SU (from DB)

                //Extract roles from JWT and convert the roles into Java Spring Authorities
                List<String> roles = this.jwtService.extractRoles(token);
                List<SimpleGrantedAuthority> authorities = roles != null
                        ? roles.stream().map(SimpleGrantedAuthority::new).toList()
                        : List.of();

                // Build authentication token with user authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId, // <-- The authenticated UUID principal !
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Feed the SecurityContextHolder so Spring Security recognizes the authenticated user
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 5: Continue filter chain execution
        filterChain.doFilter(request, response);
    }
}
