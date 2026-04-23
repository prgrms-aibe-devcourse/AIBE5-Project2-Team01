package com.example.meetball.global.auth;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.service.ProfileService;
import com.example.meetball.domain.catalog.service.CatalogService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class CurrentProfileModelAdvice {

    private final ProfileService profileService;
    private final CatalogService catalogService;

    @Value("${google.client.id:}")
    private String googleClientId;

    @ModelAttribute
    public void addCurrentProfile(Model model, HttpSession session) {
        Profile currentProfile = resolveCurrentProfile(session);
        model.addAttribute("currentProfile", currentProfile);
        model.addAttribute("isLoggedIn", currentProfile != null);
        model.addAttribute("googleClientId", googleClientId);
        model.addAttribute("googleLoginEnabled", googleClientId != null && !googleClientId.isBlank());
        model.addAttribute("meetballPositionOptions", catalogService.positionOptions());
        model.addAttribute("meetballTechStackOptions", catalogService.techStackOptions());
        model.addAttribute("meetballTechStackCategories", catalogService.techStackCategories());
        model.addAttribute("meetballTechStackMeta", catalogService.techStackMeta());
    }

    private Profile resolveCurrentProfile(HttpSession session) {
        Object profileId = session.getAttribute("profileId");
        if (!(profileId instanceof Long)) {
            return null;
        }

        try {
            return profileService.getProfileById((Long) profileId);
        } catch (IllegalArgumentException exception) {
            session.removeAttribute("profileId");
            return null;
        }
    }
}
