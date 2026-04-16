package com.example.meetball.domain.bookmark.controller;

import com.example.meetball.domain.bookmark.dto.BookmarkResponseDto;
import com.example.meetball.domain.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 찜 상태 확인 (페이지 로드 시 초기 하트 렌더링용)
    @GetMapping
    public ResponseEntity<BookmarkResponseDto> getBookmarkStatus(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "GUEST") String userNickname) {
        return ResponseEntity.ok(bookmarkService.getBookmarkStatus(projectId, userNickname));
    }

    // 찜 추가 / 취소 (토글)
    @PostMapping
    public ResponseEntity<?> toggleBookmark(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "GUEST") String userNickname) {
        try {
            BookmarkResponseDto responseDto = bookmarkService.toggleBookmark(projectId, userNickname);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            // 비회원이 누른 경우 예외 메시지 반환
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
