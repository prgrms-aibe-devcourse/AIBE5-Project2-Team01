package com.example.meetball.domain.people.controller;

import com.example.meetball.domain.people.dto.PeopleProfileResponse;
import com.example.meetball.domain.people.dto.PeopleProjectResponse;
import com.example.meetball.domain.people.service.PeopleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PeopleController {

    private final PeopleService peopleService;

    @GetMapping("/people/{userId}")
    public String profilePage(
            @PathVariable("userId") Long userId,
            @SessionAttribute(name = "userId", required = false) Long sessionUserId,
        Model model) {
        if (sessionUserId == null) {
            return "redirect:/?login=1&redirect=/people/" + userId;
        }
        if (userId.equals(sessionUserId)) {
            return "redirect:/user/mypage";
        }

        try {
            List<PeopleProjectResponse> projects = peopleService.getProjects(userId, sessionUserId);

            model.addAttribute("profile", peopleService.getProfile(userId, sessionUserId));
            model.addAttribute("projects", projects);
            model.addAttribute("leaderProjects", projects.stream()
                    .filter(project -> !project.isCompleted())
                    .filter(project -> "LEADER".equals(project.getUserRole()))
                    .toList());
            model.addAttribute("memberProjects", projects.stream()
                    .filter(project -> !project.isCompleted())
                    .filter(project -> "MEMBER".equals(project.getUserRole()))
                    .toList());
            model.addAttribute("completedProjects", projects.stream()
                    .filter(PeopleProjectResponse::isCompleted)
                    .toList());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return "people/detail";
    }

    @ResponseBody
    @GetMapping("/api/people/{userId}/profile")
    public ResponseEntity<PeopleProfileResponse> getProfile(
            @PathVariable("userId") Long userId,
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long viewerId = requireSessionUser(sessionUserId);
        return ResponseEntity.ok(peopleService.getProfile(userId, viewerId));
    }

    @ResponseBody
    @GetMapping("/api/people/{userId}/projects")
    public ResponseEntity<List<PeopleProjectResponse>> getProjects(
            @PathVariable("userId") Long userId,
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long viewerId = requireSessionUser(sessionUserId);
        return ResponseEntity.ok(peopleService.getProjects(userId, viewerId));
    }

    private Long requireSessionUser(Long sessionUserId) {
        if (sessionUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return sessionUserId;
    }
}
