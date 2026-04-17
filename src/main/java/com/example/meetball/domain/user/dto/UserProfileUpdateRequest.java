package com.example.meetball.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    private String nickname;
    private String jobTitle;
    private String techStack;
    private boolean isPublic;
}
