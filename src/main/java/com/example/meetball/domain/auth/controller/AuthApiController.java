package com.example.meetball.domain.auth.controller;

import com.example.meetball.domain.auth.dto.AuthRequestDto;
import com.example.meetball.domain.auth.dto.AuthResponseDto;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final ProfileService profileService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDto> loginWithGoogle(@RequestBody AuthRequestDto requestDto, HttpServletRequest request) {
        if (requestDto.getCredential() == null || requestDto.getCredential().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Verify Google Token and fetch or create Profile
            ProfileService.GoogleLoginResult loginResult = profileService.processGoogleLogin(requestDto.getCredential());
            Profile profile = loginResult.profile();

            // Initialize Http Session
            HttpSession session = request.getSession(true);
            session.setAttribute("accountId", profile.getAccountId());
            session.setAttribute("profileId", profile.getId());
            session.setAttribute("profileNickname", profile.getNickname());
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    profile.getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + profile.getRole()))
            );
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            if (loginResult.newlyCreated()) {
                session.setAttribute("needsProfile", true);
            } else {
                session.removeAttribute("needsProfile");
            }

            return ResponseEntity.ok(new AuthResponseDto(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> getCurrentProfile(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("profileId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long profileId = (Long) session.getAttribute("profileId");
        Profile profile = profileService.getProfileById(profileId);

        return ResponseEntity.ok(new AuthResponseDto(profile));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}
