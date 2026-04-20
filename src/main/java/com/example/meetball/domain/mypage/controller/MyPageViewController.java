package com.example.meetball.domain.mypage.controller;

import com.example.meetball.domain.mypage.dto.MyPageProfileResponse;
import com.example.meetball.domain.mypage.service.MyPageService;
import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class MyPageViewController {

    private final MyPageService myPageService;
    private final UserService userService;

    @GetMapping("/mypage")
    public String myPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long viewerId,
            Model model) {

        Long resolvedUserId = resolveUserId(userId);
        if (resolvedUserId == null) {
            return renderEmptyMyPage(model, "등록된 사용자 정보가 아직 없습니다.");
        }

        Long resolvedViewerId = viewerId != null ? viewerId : resolvedUserId;

        try {
            MyPageProfileResponse profile = myPageService.getMyProfile(resolvedUserId, resolvedViewerId);
            List<ParticipatedProjectResponse> projects = myPageService.getMyProjects(resolvedUserId, resolvedViewerId);

            model.addAttribute("profile", profile);
            model.addAttribute("participatedProjects", projects);
            model.addAttribute("isOwner", profile.isOwner());
        } catch (IllegalArgumentException e) {
            return renderEmptyMyPage(model, e.getMessage());
        }

        return "user/mypage";
    }

    private Long resolveUserId(Long userId) {
        if (userId != null) {
            return userId;
        }

        return userService.findDefaultUser()
                .map(User::getId)
                .orElse(null);
    }

    private String renderEmptyMyPage(Model model, String message) {
        MyPageProfileResponse profile = MyPageProfileResponse.builder()
                .nickname("게스트")
                .email("-")
                .jobTitle("로그인 또는 사용자 등록이 필요합니다.")
                .techStack("-")
                .role("GUEST")
                .isPublic(true)
                .meetBallIndex(0)
                .isOwner(false)
                .build();

        model.addAttribute("profile", profile);
        model.addAttribute("participatedProjects", Collections.emptyList());
        model.addAttribute("isOwner", false);
        model.addAttribute("mypageNotice", message);

        return "user/mypage";
    }
}
