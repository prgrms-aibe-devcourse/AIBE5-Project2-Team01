package com.example.meetball.domain.bookmarkedproject.service;

import com.example.meetball.domain.bookmarkedproject.dto.BookmarkedProjectResponse;
import com.example.meetball.domain.bookmarkedproject.dto.BookmarkedProjectStatusResponseDto;
import com.example.meetball.domain.bookmarkedproject.entity.BookmarkedProject;
import com.example.meetball.domain.bookmarkedproject.repository.BookmarkedProjectRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkedProjectService {

    private final BookmarkedProjectRepository bookmarkedProjectRepository;
    private final ProjectRepository projectRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public BookmarkedProjectStatusResponseDto toggleBookmark(Long projectId, Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        Optional<BookmarkedProject> existingBookmark = bookmarkedProjectRepository.findByProjectAndProfile(project, profile);
        boolean isBookmarked;

        if (existingBookmark.isPresent()) {
            bookmarkedProjectRepository.delete(existingBookmark.get());
            project.decrementBookmarkCount();
            isBookmarked = false;
        } else {
            BookmarkedProject bookmark = BookmarkedProject.builder()
                    .project(project)
                    .profile(profile)
                    .build();
            bookmarkedProjectRepository.save(bookmark);
            project.incrementBookmarkCount();
            isBookmarked = true;
        }

        int totalBookmarks = bookmarkedProjectRepository.countByProject(project);
        return new BookmarkedProjectStatusResponseDto(isBookmarked, totalBookmarks);
    }

    @Transactional(readOnly = true)
    public List<BookmarkedProjectResponse> getBookmarkedProjects(Profile profile) {
        return bookmarkedProjectRepository.findByProfile(profile).stream()
                .map(BookmarkedProjectResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookmarkedProjectStatusResponseDto getBookmarkStatus(Long projectId, Long profileId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        if (profileId == null) {
            return new BookmarkedProjectStatusResponseDto(false, bookmarkedProjectRepository.countByProject(project));
        }

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean isBookmarked = bookmarkedProjectRepository.findByProjectAndProfile(project, profile).isPresent();
        int totalBookmarks = bookmarkedProjectRepository.countByProject(project);
        return new BookmarkedProjectStatusResponseDto(isBookmarked, totalBookmarks);
    }
}
