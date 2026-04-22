package com.example.meetball.domain.mypage.controller;

import com.example.meetball.domain.mypage.dto.MyPageProfileResponse;
import com.example.meetball.domain.mypage.service.MyPageService;
import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class MyPageViewController {

    private final MyPageService myPageService;

    @GetMapping("/mypage")
    public String myPage(
            @RequestParam(required = false) Long userId,
            @SessionAttribute(name = "userId", required = false) Long sessionUserId,
            HttpSession session,
            Model model) {

        if (sessionUserId == null) {
            if (userId != null) {
                return "redirect:/?login=1&redirect=/people/" + userId;
            }
            return "redirect:/?login=1&redirect=/user/mypage";
        }

        if (userId != null && !userId.equals(sessionUserId)) {
            return "redirect:/people/" + userId;
        }

        try {
            MyPageProfileResponse profile = myPageService.getMyProfile(sessionUserId, sessionUserId);
            List<ParticipatedProjectResponse> projects = myPageService.getMyProjects(sessionUserId, sessionUserId);

            model.addAttribute("profile", profile);
            model.addAttribute("participatedProjects", projects);
            model.addAttribute("isOwner", profile.isOwner());
        } catch (IllegalArgumentException e) {
            session.invalidate();
            return "redirect:/?login=1&redirect=/user/mypage";
        }

        return "user/mypage";
    }
}
