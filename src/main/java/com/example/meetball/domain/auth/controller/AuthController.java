package com.example.meetball.domain.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Value("${google.client.id:}")
    private String googleClientId;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("googleClientId", googleClientId);
        model.addAttribute("googleLoginEnabled", googleClientId != null && !googleClientId.isBlank());
        return "auth/login";
    }
}
