package com.mobile.server.domain.auth.jwt;

import com.mobile.server.domain.auth.dto.UserDto;
import com.mobile.server.domain.auth.entity.RoleType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        try {
            if (!jwtService.validateToken(token)) {
                chain.doFilter(request, response);
                return;
            }

            Long userId = jwtService.getUserIdFromToken(token);
            String role = jwtService.getRoleFromToken(token);

            UserDto principal = new UserDto();
            principal.setId(userId);
            principal.setRole(RoleType.valueOf(role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(request, response);
    }
}
