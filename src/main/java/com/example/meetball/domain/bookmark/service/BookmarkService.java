package com.example.meetball.domain.bookmark.service;

import com.example.meetball.domain.bookmark.dto.BookmarkResponseDto;
import com.example.meetball.domain.bookmark.entity.Bookmark;
import com.example.meetball.domain.bookmark.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public BookmarkResponseDto toggleBookmark(Long projectId, String userNickname) {
        // 회원/비회원 권한 체크 (예외 처리)
        if ("GUEST".equals(userNickname) || userNickname == null || userNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("로그인이 필요한 기능입니다.");
        }

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByProjectIdAndUserNickname(projectId, userNickname);
        boolean isBookmarked;

        if (existingBookmark.isPresent()) {
            // 이미 찜한 상태면 취소(삭제)
            bookmarkRepository.delete(existingBookmark.get());
            isBookmarked = false;
        } else {
            // 안 찜한 상태면 추가
            Bookmark bookmark = Bookmark.builder()
                    .projectId(projectId)
                    .userNickname(userNickname)
                    .build();
            bookmarkRepository.save(bookmark);
            isBookmarked = true;
        }

        int totalBookmarks = bookmarkRepository.countByProjectId(projectId);
        return new BookmarkResponseDto(isBookmarked, totalBookmarks);
    }
    
    @Transactional(readOnly = true)
    public BookmarkResponseDto getBookmarkStatus(Long projectId, String userNickname) {
        boolean isBookmarked = bookmarkRepository.findByProjectIdAndUserNickname(projectId, userNickname).isPresent();
        int totalBookmarks = bookmarkRepository.countByProjectId(projectId);
        return new BookmarkResponseDto(isBookmarked, totalBookmarks);
    }
}
