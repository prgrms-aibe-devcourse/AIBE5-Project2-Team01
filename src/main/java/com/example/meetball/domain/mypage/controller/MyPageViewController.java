package com.example.meetball.domain.mypage.controller;

import com.example.meetball.domain.mypage.dto.MyPageProfileResponse;
import com.example.meetball.domain.mypage.service.MyPageService;
import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyPageViewController {

    private final MyPageService myPageService;

    @GetMapping("/mypage")
    public String myPage(
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId,
            HttpSession session,
            Model model) {

        if (sessionProfileId == null) {
            return "redirect:/?login=1&redirect=/mypage";
        }

        try {
            MyPageProfileResponse profile = myPageService.getMyProfile(sessionProfileId, sessionProfileId);
            List<ParticipatedProjectResponse> projects = myPageService.getMyProjects(sessionProfileId, sessionProfileId);

            model.addAttribute("profile", profile);
            model.addAttribute("participatedProjects", projects);
            model.addAttribute("isOwner", profile.isOwner());
        } catch (IllegalArgumentException e) {
            session.invalidate();
            return "redirect:/?login=1&redirect=/mypage";
        }

        return "mypage/mypage";
    }
}
