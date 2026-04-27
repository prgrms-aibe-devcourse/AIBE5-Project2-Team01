package com.example.meetball.domain.bookmarkedproject.controller;

import com.example.meetball.domain.bookmarkedproject.dto.BookmarkedProjectStatusResponseDto;
import com.example.meetball.domain.bookmarkedproject.service.BookmarkedProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/projects/{projectId}/bookmarks")
@RequiredArgsConstructor
public class BookmarkedProjectController {

    private final BookmarkedProjectService bookmarkedProjectService;

    // 찜 상태 확인 (페이지 로드 시 초기 하트 렌더링용)
    @GetMapping
    public ResponseEntity<BookmarkedProjectStatusResponseDto> getBookmarkStatus(
            @PathVariable Long projectId,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        return ResponseEntity.ok(bookmarkedProjectService.getBookmarkStatus(projectId, profileId));
    }

    // 찜 추가 / 취소 (토글)
    @PostMapping
    public ResponseEntity<?> toggleBookmark(
            @PathVariable Long projectId,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        try {
            BookmarkedProjectStatusResponseDto responseDto = bookmarkedProjectService.toggleBookmark(projectId, profileId);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            // 비회원 혹은 유저를 찾을 수 없는 경우 예외 메시지 반환
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
