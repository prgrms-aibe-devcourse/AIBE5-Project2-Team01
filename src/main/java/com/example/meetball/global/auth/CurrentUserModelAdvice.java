package com.example.meetball.global.auth;

import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.service.UserService;
import com.example.meetball.domain.catalog.service.CatalogService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserModelAdvice {

    private final UserService userService;
    private final CatalogService catalogService;

    @Value("${google.client.id:}")
    private String googleClientId;

    @ModelAttribute
    public void addCurrentUser(Model model, HttpSession session) {
        User currentUser = resolveCurrentUser(session);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isLoggedIn", currentUser != null);
        model.addAttribute("googleClientId", googleClientId);
        model.addAttribute("googleLoginEnabled", googleClientId != null && !googleClientId.isBlank());
        model.addAttribute("meetballPositionOptions", catalogService.positionOptions());
        model.addAttribute("meetballTechStackOptions", catalogService.techStackOptions());
        model.addAttribute("meetballTechStackCategories", catalogService.techStackCategories());
        model.addAttribute("meetballTechStackMeta", catalogService.techStackMeta());
    }

    private User resolveCurrentUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (!(userId instanceof Long)) {
            return null;
        }

        try {
            return userService.getUserById((Long) userId);
        } catch (IllegalArgumentException exception) {
            session.removeAttribute("userId");
            return null;
        }
    }
}
