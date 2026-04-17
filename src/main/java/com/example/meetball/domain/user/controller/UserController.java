package com.example.meetball.domain.user.controller;

import com.example.meetball.domain.user.dto.UserProfileUpdateRequest;
import com.example.meetball.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // TODO: 현재는 임시로 파라미터로 userId를 받지만, 추후 @AuthenticationPrincipal로 변경
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @RequestParam Long userId,
            @RequestBody UserProfileUpdateRequest request) {
        
        userService.updateUserProfile(userId, request);
        return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.");
    }
}
