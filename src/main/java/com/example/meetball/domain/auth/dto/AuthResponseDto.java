package com.example.meetball.domain.auth.dto;

import com.example.meetball.domain.profile.entity.Profile;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String role;

    public AuthResponseDto(Profile profile) {
        this.id = profile.getId();
        this.email = profile.getEmail();
        this.nickname = profile.getNickname();
        this.role = profile.getRole();
    }
}
