package com.example.meetball.global.auth;

import com.example.meetball.domain.profile.repository.ProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SessionProfileAuthenticationFilter extends OncePerRequestFilter {

    private static final String DEFAULT_MEMBER_ROLE = "MEMBER";
    private final ProfileRepository profileRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!hasAuthentication()) {
            restoreAuthenticationFromSession(request);
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private void restoreAuthenticationFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long profileId = extractProfileId(session);
        if (session == null || profileId == null) {
            return;
        }

        profileRepository.findById(profileId).ifPresent(profile -> {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    profile.getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + DEFAULT_MEMBER_ROLE))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        });
    }

    private Long extractProfileId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("profileId");
        if (value instanceof Long profileId) {
            return profileId;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

}
