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

    @GetMapping("/people/{profileId}")
    public String profilePage(
            @PathVariable("profileId") Long profileId,
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId,
        Model model) {
        if (sessionProfileId == null) {
            return "redirect:/?login=1&redirect=/people/" + profileId;
        }
        if (profileId.equals(sessionProfileId)) {
            return "redirect:/mypage";
        }

        try {
            List<PeopleProjectResponse> projects = peopleService.getProjects(profileId, sessionProfileId);

            model.addAttribute("profile", peopleService.getProfile(profileId, sessionProfileId));
            model.addAttribute("projects", projects);
            model.addAttribute("leaderProjects", projects.stream()
                    .filter(project -> !"COMPLETED".equals(project.getProgressStatus()))
                    .filter(project -> "LEADER".equals(project.getParticipantRole()))
                    .toList());
            model.addAttribute("memberProjects", projects.stream()
                    .filter(project -> !"COMPLETED".equals(project.getProgressStatus()))
                    .filter(project -> "MEMBER".equals(project.getParticipantRole()))
                    .toList());
            model.addAttribute("completedProjects", projects.stream()
                    .filter(project -> "COMPLETED".equals(project.getProgressStatus()))
                    .toList());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return "people/detail";
    }

    @ResponseBody
    @GetMapping("/api/people/{profileId}/profile")
    public ResponseEntity<PeopleProfileResponse> getProfile(
            @PathVariable("profileId") Long profileId,
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId) {
        Long viewerId = requireSignedInProfileId(sessionProfileId);
        return ResponseEntity.ok(peopleService.getProfile(profileId, viewerId));
    }

    @ResponseBody
    @GetMapping("/api/people/{profileId}/projects")
    public ResponseEntity<List<PeopleProjectResponse>> getProjects(
            @PathVariable("profileId") Long profileId,
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId) {
        Long viewerId = requireSignedInProfileId(sessionProfileId);
        return ResponseEntity.ok(peopleService.getProjects(profileId, viewerId));
    }

    private Long requireSignedInProfileId(Long sessionProfileId) {
        if (sessionProfileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return sessionProfileId;
    }
}
