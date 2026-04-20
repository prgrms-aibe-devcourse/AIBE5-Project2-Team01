package com.example.meetball.domain.mypage.controller;

import com.example.meetball.domain.mypage.dto.MyPageProfileResponse;
import com.example.meetball.domain.mypage.service.MyPageService;
import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class MyPageViewController {

    private final MyPageService myPageService;

    @GetMapping("/mypage")
    public String myPage(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(required = false) Long viewerId,
            Model model) {
        
        // 1. 프로필 정보 가져오기
        MyPageProfileResponse profile = myPageService.getMyProfile(userId, viewerId);
        
        // 2. 활동 내역 가져오기 (전체 프로젝트)
        List<ParticipatedProjectResponse> projects = myPageService.getMyProjects(userId, viewerId);

        // 3. 모델(바구니)에 담기
        model.addAttribute("profile", profile);
        model.addAttribute("participatedProjects", projects);
        model.addAttribute("isOwner", profile.isOwner());

        // 4. templates/user/mypage.html 반환
        return "user/mypage";
    }
}
