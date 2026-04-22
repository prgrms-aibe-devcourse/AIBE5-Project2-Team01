package com.example.meetball.global.auth;

import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserModelAdvice {

    private final UserService userService;

    @ModelAttribute
    public void addCurrentUser(Model model, HttpSession session) {
        User currentUser = resolveCurrentUser(session);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isLoggedIn", currentUser != null);
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
