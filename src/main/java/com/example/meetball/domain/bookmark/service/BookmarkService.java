package com.example.meetball.domain.bookmark.service;

import com.example.meetball.domain.bookmark.dto.BookmarkedProjectResponse;
import com.example.meetball.domain.bookmark.dto.BookmarkResponseDto;
import com.example.meetball.domain.bookmark.entity.Bookmark;
import com.example.meetball.domain.bookmark.repository.BookmarkRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookmarkResponseDto toggleBookmark(Long projectId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByProjectAndUser(project, user);
        boolean isBookmarked;

        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            isBookmarked = false;
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .project(project)
                    .user(user)
                    .build();
            bookmarkRepository.save(bookmark);
            isBookmarked = true;
        }

        int totalBookmarks = bookmarkRepository.countByProject(project);
        return new BookmarkResponseDto(isBookmarked, totalBookmarks);
    }

    @Transactional(readOnly = true)
    public List<BookmarkedProjectResponse> getBookmarkedProjects(User user) {
        return bookmarkRepository.findByUser(user).stream()
                .map(BookmarkedProjectResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookmarkResponseDto getBookmarkStatus(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        if (userId == null) {
            return new BookmarkResponseDto(false, bookmarkRepository.countByProject(project));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean isBookmarked = bookmarkRepository.findByProjectAndUser(project, user).isPresent();
        int totalBookmarks = bookmarkRepository.countByProject(project);
        return new BookmarkResponseDto(isBookmarked, totalBookmarks);
    }
}
