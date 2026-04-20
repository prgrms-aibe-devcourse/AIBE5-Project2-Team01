package com.example.meetball.domain.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookmarkResponseDto {
    private boolean isBookmarked; // 현재 사용자가 찜했는지 여부
    private int totalBookmarks;   // 프로젝트의 총 찜 개수
}
