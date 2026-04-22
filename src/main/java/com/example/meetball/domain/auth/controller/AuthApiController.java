package com.example.meetball.domain.auth.controller;

import com.example.meetball.domain.auth.dto.AuthRequestDto;
import com.example.meetball.domain.auth.dto.AuthResponseDto;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.service.UserService;
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

    private final UserService userService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDto> loginWithGoogle(@RequestBody AuthRequestDto requestDto, HttpServletRequest request) {
        if (requestDto.getCredential() == null || requestDto.getCredential().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Verify Google Token and fetch or create User
            User user = userService.processGoogleLogin(requestDto.getCredential());

            // Initialize Http Session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userNickname", user.getNickname());
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            );
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            String nickname = user.getNickname();
            boolean needsProfile = user.getJobTitle() == null
                    || "-".equals(user.getJobTitle())
                    || nickname == null
                    || nickname.isBlank()
                    || nickname.startsWith("User_");

            if (needsProfile) {
                session.setAttribute("needsProfile", true);
            } else {
                session.removeAttribute("needsProfile");
            }

            return ResponseEntity.ok(new AuthResponseDto(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);

        return ResponseEntity.ok(new AuthResponseDto(user));
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
