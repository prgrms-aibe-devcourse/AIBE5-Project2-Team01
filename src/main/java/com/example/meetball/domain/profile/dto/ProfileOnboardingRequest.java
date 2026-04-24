package com.example.meetball.domain.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileOnboardingRequest {

    private String name;
    private String nickname;
    private String phoneNumber;
    private LocalDate birthDate;
    private String gender;
    private String position;
    private String experienceYears;
    private String organization;
    private boolean orgVisible;
    private List<String> techStacks = List.of();

    public List<String> getTechStacks() {
        return techStacks == null ? List.of() : techStacks;
    }
}
