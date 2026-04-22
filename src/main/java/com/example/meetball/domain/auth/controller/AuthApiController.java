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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UserService userService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDto> loginWithGoogle(@RequestBody AuthRequestDto requestDto, HttpServletRequest request) {
        if (requestDto.getCredential() == null || requestDto.getCredential().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google ID Token is missing.");
        }

        try {
            // Verify Google Token and fetch or create User
            User user = userService.processGoogleLogin(requestDto.getCredential());

            // Initialize Http Session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());

            return ResponseEntity.ok(new AuthResponseDto(user));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing login");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated.");
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
        return ResponseEntity.ok().build();
    }
}
